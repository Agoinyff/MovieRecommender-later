package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import com.rcd.movierecommender.backend.mapper.RatingMapper;
import com.rcd.movierecommender.backend.service.recommender.slopeone.CustomSlopeOneRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;
    private final CacheManager cacheManager;

    /**
     * 推荐服务核心。
     * 负责构建 Mahout 推荐器，并在协同过滤无结果时回退到偏好兜底推荐。
     */
    public RecommendationService(RatingMapper ratingMapper,
            MovieMapper movieMapper,
            CacheManager cacheManager) {
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
        this.cacheManager = cacheManager;
    }

    /**
     * 为指定用户生成推荐结果。
     *
     * @param userId 目标用户 ID。
     * @param size 返回条数。
     * @param strategy 推荐策略。
     * @return 推荐结果列表。
     */
    @Cacheable(value = "recommendations", key = "#userId + '_' + #size + '_' + #strategy", unless = "#result.isEmpty()")
    public List<RecommendationDto> recommend(Long userId, int size, RecommendationStrategy strategy) {
        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();

        log.info("[Performance] recommendation request: userId={}, size={}, strategy={}", userId, size, strategy);

        try {
            DataModel dataModel = getDataModelFromCache();
            if (dataModel == null) {
                log.warn("Data model is not ready. Try again later or check /api/model/status.");
                return fallbackRecommend(userId, size);
            }

            try {
                log.info("Data model stats: users={}, items={}", dataModel.getNumUsers(), dataModel.getNumItems());
            } catch (TasteException ex) {
                log.error("Failed to read data model stats", ex);
            }

            if (!userExists(userId, dataModel)) {
                log.warn("User {} is not present in the data model. Use fallback recommendations.", userId);
                return fallbackRecommend(userId, size);
            }

            List<RecommendedItem> recommendedItems = buildRecommender(dataModel, strategy).recommend(userId, size);
            List<RecommendationDto> results = batchToRecommendationDtos(recommendedItems);
            if (results.isEmpty()) {
                log.info("Collaborative filtering returned no results. Falling back to preference-based recommendations for user {}.", userId);
                results = fallbackRecommend(userId, size);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            long memoryUsed = (getUsedMemory() - startMemory) / (1024 * 1024);
            log.info("[Performance] recommendation completed: elapsed={}ms, memoryDelta={}MB, resultCount={}",
                    elapsedTime, memoryUsed, results.size());
            return results;
        } catch (TasteException ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[Performance] recommendation failed: elapsed={}ms", elapsedTime, ex);
            throw new BusinessException(
                    ErrorCode.RECOMMENDATION_ENGINE_ERROR,
                    "Mahout recommendation failed: " + ex.getMessage(),
                    ex,
                    buildRootCause(ex));
        }
    }

    /**
     * 获取当前 JVM 已使用内存。
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 从缓存中读取全局数据模型，不主动触发构建。
     */
    private DataModel getDataModelFromCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get("global");
        return wrapper == null ? null : (DataModel) wrapper.get();
    }

    /**
     * 构建 Mahout 数据模型。
     * 采用流式读取评分数据，尽量降低大数据量下的一次性内存压力。
     */
    public DataModel buildDataModel() {
        try {
            log.info("Start building Mahout data model with streaming load...");
            long startTime = System.currentTimeMillis();

            FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<>();
            Map<Long, List<Preference>> preferences = new HashMap<>();
            java.util.concurrent.atomic.AtomicLong loadedCount = new java.util.concurrent.atomic.AtomicLong(0);
            java.util.concurrent.atomic.AtomicLong lastLogTime = new java.util.concurrent.atomic.AtomicLong(startTime);

            ratingMapper.streamAllRatings(resultContext -> {
                RatingEntity rating = resultContext.getResultObject();
                preferences
                        .computeIfAbsent(rating.getUserId(), id -> new ArrayList<>())
                        .add(new GenericPreference(
                                rating.getUserId(),
                                rating.getMovieId(),
                                rating.getPreference().floatValue()));

                long count = loadedCount.incrementAndGet();
                if (count % 100000 == 0) {
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastLogTime.get();
                    double speed = 100000.0 / (elapsed / 1000.0);
                    log.info("Loaded {} ratings, last 100000 rows took {}ms, speed={} rows/s",
                            count, elapsed, Math.round(speed));
                    lastLogTime.set(now);
                }
            });

            log.info("Ratings loaded: count={}, users={}", loadedCount.get(), preferences.size());

            for (Map.Entry<Long, List<Preference>> entry : preferences.entrySet()) {
                preferenceMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
            }
            preferences.clear();

            long endTime = System.currentTimeMillis();
            log.info("Data model build completed in {}s", (endTime - startTime) / 1000.0);
            return new GenericDataModel(preferenceMap);
        } catch (DataAccessException ex) {
            throw new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Failed to load ratings for data model build",
                    ex,
                    buildRootCause(ex));
        } catch (OutOfMemoryError ex) {
            log.error("Out of memory while building recommendation data model", ex);
            throw new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Not enough memory to build the recommendation data model",
                    ex);
        }
    }

    /**
     * 判断用户是否已经进入数据模型。
     */
    private boolean userExists(Long userId, DataModel dataModel) {
        try {
            PreferenceArray prefs = dataModel.getPreferencesFromUser(userId);
            log.debug("User {} has {} ratings in data model", userId, prefs.length());
            return true;
        } catch (TasteException ex) {
            log.debug("User {} is not present in data model: {}", userId, ex.getMessage());
            return false;
        }
    }

    /**
     * 根据策略构建推荐器。
     * 这里改为 if/else 实现，避免在 Java 8 运行环境下生成额外的 enum switch 辅助类。
     */
    private Recommender buildRecommender(DataModel dataModel, RecommendationStrategy strategy) throws TasteException {
        if (RecommendationStrategy.ITEM_BASED == strategy) {
            ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
            return new GenericItemBasedRecommender(dataModel, itemSimilarity);
        }
        if (RecommendationStrategy.SLOPE_ONE == strategy) {
            return new CustomSlopeOneRecommender(dataModel);
        }
        UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, userSimilarity, dataModel);
        return new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
    }

    /**
     * 批量补齐电影元数据，避免推荐结果逐条查询导致的 N+1 问题。
     */
    private List<RecommendationDto> batchToRecommendationDtos(List<RecommendedItem> recommendedItems) {
        if (recommendedItems.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<Long> movieIds = recommendedItems.stream()
                    .map(RecommendedItem::getItemID)
                    .collect(Collectors.toList());

            List<MovieEntity> movies = movieIds.isEmpty() ? Collections.emptyList() : movieMapper.findByIds(movieIds);
            Map<Long, MovieEntity> movieMap = movies.stream()
                    .collect(Collectors.toMap(MovieEntity::getId, movie -> movie));

            return recommendedItems.stream()
                    .map(item -> {
                        MovieEntity movie = movieMap.get(item.getItemID());
                        if (movie == null) {
                            return null;
                        }
                        return new RecommendationDto(
                                movie.getId(),
                                movie.getName(),
                                movie.getPublishedYear(),
                                movie.getGenres(),
                                movie.getPosterUrl(),
                                item.getValue());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Failed to batch load movie metadata for recommendations",
                    ex,
                    buildRootCause(ex));
        }
    }

    /**
     * 协同过滤没有产出结果时的兜底方案。
     * 优先根据用户高分电影的类型扩展候选集，不足时再补最近电影。
     */
    private List<RecommendationDto> fallbackRecommend(Long userId, int size) {
        List<RatingEntity> userRatings = ratingMapper.findByUserId(userId);
        if (userRatings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ratedMovieIds = userRatings.stream()
                .map(RatingEntity::getMovieId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> priorityMovieIds = userRatings.stream()
                .filter(rating -> rating.getPreference() != null && rating.getPreference() >= 4.0)
                .map(RatingEntity::getMovieId)
                .distinct()
                .collect(Collectors.toList());
        if (priorityMovieIds.isEmpty()) {
            priorityMovieIds = ratedMovieIds;
        }

        List<MovieEntity> ratedMovies = movieMapper.findByIds(priorityMovieIds);
        List<String> preferredGenres = extractPreferredGenres(ratedMovies);

        List<MovieEntity> candidates = movieMapper.findMoviesByGenresExcludingIds(preferredGenres, ratedMovieIds, size * 2);
        if (candidates.size() < size) {
            List<MovieEntity> recentMovies = movieMapper.findRecentMoviesExcludingIds(ratedMovieIds, size * 2);
            mergeCandidates(candidates, recentMovies, size * 2);
        }

        return candidates.stream()
                .limit(size)
                .map(movie -> new RecommendationDto(
                        movie.getId(),
                        movie.getName(),
                        movie.getPublishedYear(),
                        movie.getGenres(),
                        movie.getPosterUrl(),
                        0.0))
                .collect(Collectors.toList());
    }

    /**
     * 提取用户高分电影里的偏好类型。
     */
    private List<String> extractPreferredGenres(List<MovieEntity> movies) {
        Set<String> genres = new LinkedHashSet<>();
        for (MovieEntity movie : movies) {
            if (movie == null || movie.getGenres() == null) {
                continue;
            }
            String[] parts = movie.getGenres().split("[|,/\\s]+");
            for (String part : parts) {
                String genre = part.trim();
                if (!genre.isEmpty()) {
                    genres.add(genre);
                }
                if (genres.size() >= 6) {
                    return new ArrayList<>(genres);
                }
            }
        }
        return new ArrayList<>(genres);
    }

    /**
     * 合并候选电影并去重。
     */
    private void mergeCandidates(List<MovieEntity> base, List<MovieEntity> extra, int limit) {
        Set<Long> existingIds = base.stream().map(MovieEntity::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        for (MovieEntity movie : extra) {
            if (existingIds.add(movie.getId())) {
                base.add(movie);
            }
            if (base.size() >= limit) {
                break;
            }
        }
    }

    @Deprecated
    private RecommendationDto toRecommendationDto(Long movieId, double score) {
        try {
            MovieEntity movie = movieMapper.findById(movieId);
            if (movie == null) {
                return null;
            }
            return new RecommendationDto(
                    movie.getId(),
                    movie.getName(),
                    movie.getPublishedYear(),
                    movie.getGenres(),
                    movie.getPosterUrl(),
                    score);
        } catch (DataAccessException ex) {
            throw new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Failed to load movie metadata",
                    ex,
                    buildRootCause(ex));
        }
    }

    private List<String> buildRootCause(Throwable ex) {
        List<String> details = new ArrayList<>();
        Throwable cause = ex.getCause();
        while (cause != null) {
            details.add(cause.getMessage());
            cause = cause.getCause();
        }
        return details;
    }
}