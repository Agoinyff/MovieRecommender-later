package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieRecommendationDto;
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
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final int MIN_RATINGS_FOR_PERSONALIZATION = 5;

    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;
    private final CacheManager cacheManager;

    public RecommendationService(RatingMapper ratingMapper, MovieMapper movieMapper, CacheManager cacheManager) {
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
        this.cacheManager = cacheManager;
    }

    public List<RecommendationDto> recommendWithFallback(Long userId, int size, RecommendationStrategy strategy) {
        if (ratingMapper.countByUserId(userId) < MIN_RATINGS_FOR_PERSONALIZATION) {
            return getPopularRecommendations(size);
        }
        List<RecommendationDto> personalized = recommend(userId, size, strategy);
        if (personalized.isEmpty()) {
            return getPopularRecommendations(size);
        }
        return personalized;
    }

    public List<MovieRecommendationDto> recommendForMovie(Long userId, Long movieId, int size,
            RecommendationStrategy strategy) {
        LinkedHashMap<Long, MovieRecommendationDto> result = new LinkedHashMap<Long, MovieRecommendationDto>();
        List<RecommendationDto> personalized = recommendWithFallback(userId, Math.max(size * 2, size + 2), strategy);
        for (RecommendationDto item : personalized) {
            if (!movieId.equals(item.getMovieId())) {
                result.put(item.getMovieId(), new MovieRecommendationDto(item.getMovieId(), item.getName(),
                        item.getPublishedYear(), item.getGenres(), item.getPosterUrl(), item.getScore(),
                        strategy.name()));
            }
            if (result.size() >= size) {
                return new ArrayList<MovieRecommendationDto>(result.values());
            }
        }

        MovieEntity currentMovie = movieMapper.findById(movieId);
        if (currentMovie != null && currentMovie.getGenres() != null) {
            String primaryGenre = extractPrimaryGenre(currentMovie.getGenres());
            List<MovieEntity> similarMovies = movieMapper.findByGenresLike(movieId, primaryGenre, size * 2);
            double score = 4.0;
            for (MovieEntity movie : similarMovies) {
                if (!result.containsKey(movie.getId())) {
                    result.put(movie.getId(), new MovieRecommendationDto(movie.getId(), movie.getName(),
                            movie.getPublishedYear(), movie.getGenres(), movie.getPosterUrl(), score,
                            strategy.name()));
                    score = Math.max(1.0, score - 0.1);
                }
                if (result.size() >= size) {
                    return new ArrayList<MovieRecommendationDto>(result.values());
                }
            }
        }

        for (RecommendationDto item : getPopularRecommendations(size * 2)) {
            if (!movieId.equals(item.getMovieId()) && !result.containsKey(item.getMovieId())) {
                result.put(item.getMovieId(), new MovieRecommendationDto(item.getMovieId(), item.getName(),
                        item.getPublishedYear(), item.getGenres(), item.getPosterUrl(), item.getScore(),
                        strategy.name()));
            }
            if (result.size() >= size) {
                break;
            }
        }
        return new ArrayList<MovieRecommendationDto>(result.values());
    }

    public List<RecommendationDto> getPopularRecommendations(int size) {
        int safeSize = Math.max(1, Math.min(size, 50));
        List<MovieEntity> movies;
        try {
            movies = movieMapper.findPopularMovies(safeSize);
        } catch (DataAccessException ex) {
            log.warn("Popular recommendation query timed out, falling back to lightweight movie list", ex);
            try {
                movies = movieMapper.findFallbackMovies(safeSize);
            } catch (DataAccessException fallbackEx) {
                log.error("Fallback movie query failed", fallbackEx);
                return Collections.emptyList();
            }
        }
        List<RecommendationDto> result = new ArrayList<RecommendationDto>();
        double score = 5.0;
        for (MovieEntity movie : movies) {
            result.add(new RecommendationDto(movie.getId(), movie.getName(), movie.getPublishedYear(),
                    movie.getGenres(), movie.getPosterUrl(), score));
            score = Math.max(1.0, score - 0.1);
        }
        return result;
    }

    @Cacheable(value = "recommendations", key = "#userId + '_' + #size + '_' + #strategy", unless = "#result.isEmpty()")
    public List<RecommendationDto> recommend(Long userId, int size, RecommendationStrategy strategy) {
        try {
            DataModel dataModel = getDataModelFromCache();
            if (dataModel == null || !userExists(userId, dataModel)) {
                return Collections.emptyList();
            }
            List<RecommendedItem> recommendedItems = buildRecommender(dataModel, strategy).recommend(userId, size);
            return batchToRecommendationDtos(recommendedItems);
        } catch (TasteException ex) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_ENGINE_ERROR,
                    "閹恒劏宕樼粻妤佺《閹笛嗩攽婢惰精瑙? " + ex.getMessage(), ex, buildRootCause(ex));
        }
    }

    public DataModel buildDataModel() {
        try {
            FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<PreferenceArray>();
            Map<Long, List<Preference>> preferences = new HashMap<Long, List<Preference>>();

            ratingMapper.streamAllRatings(new org.apache.ibatis.session.ResultHandler<RatingEntity>() {
                @Override
                public void handleResult(org.apache.ibatis.session.ResultContext<? extends RatingEntity> resultContext) {
                    RatingEntity rating = resultContext.getResultObject();
                    List<Preference> userPrefs = preferences.get(rating.getUserId());
                    if (userPrefs == null) {
                        userPrefs = new ArrayList<Preference>();
                        preferences.put(rating.getUserId(), userPrefs);
                    }
                    userPrefs.add(new GenericPreference(rating.getUserId(), rating.getMovieId(),
                            rating.getPreference().floatValue()));
                }
            });

            for (Map.Entry<Long, List<Preference>> entry : preferences.entrySet()) {
                preferenceMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
            }
            return new GenericDataModel(preferenceMap);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Failed to load rating data", ex, buildRootCause(ex));
        } catch (OutOfMemoryError ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Out of memory while building recommendation model", ex);
        }
    }

    private DataModel getDataModelFromCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get("global");
        return wrapper == null ? null : (DataModel) wrapper.get();
    }

    private boolean userExists(Long userId, DataModel dataModel) {
        try {
            dataModel.getPreferencesFromUser(userId);
            return true;
        } catch (TasteException ex) {
            return false;
        }
    }

    private Recommender buildRecommender(DataModel dataModel, RecommendationStrategy strategy) throws TasteException {
        switch (strategy) {
            case ITEM_BASED:
                ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
                return new GenericItemBasedRecommender(dataModel, itemSimilarity);
            case SLOPE_ONE:
                return new CustomSlopeOneRecommender(dataModel);
            case USER_BASED:
            default:
                UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
                UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, userSimilarity, dataModel);
                return new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
        }
    }

    private List<RecommendationDto> batchToRecommendationDtos(List<RecommendedItem> recommendedItems) {
        if (recommendedItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> movieIds = recommendedItems.stream().map(new java.util.function.Function<RecommendedItem, Long>() {
            @Override
            public Long apply(RecommendedItem item) {
                return item.getItemID();
            }
        }).collect(Collectors.toList());
        List<MovieEntity> movies = movieMapper.findByIds(movieIds);
        Map<Long, MovieEntity> movieMap = movies.stream()
                .collect(Collectors.toMap(MovieEntity::getId, new java.util.function.Function<MovieEntity, MovieEntity>() {
                    @Override
                    public MovieEntity apply(MovieEntity movie) {
                        return movie;
                    }
                }));
        return recommendedItems.stream().map(new java.util.function.Function<RecommendedItem, RecommendationDto>() {
            @Override
            public RecommendationDto apply(RecommendedItem item) {
                MovieEntity movie = movieMap.get(item.getItemID());
                if (movie == null) {
                    return null;
                }
                return new RecommendationDto(movie.getId(), movie.getName(), movie.getPublishedYear(),
                        movie.getGenres(), movie.getPosterUrl(), item.getValue());
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<String> buildRootCause(Throwable ex) {
        List<String> details = new ArrayList<String>();
        Throwable cause = ex.getCause();
        while (cause != null) {
            details.add(cause.getMessage());
            cause = cause.getCause();
        }
        return details;
    }

    private String extractPrimaryGenre(String genres) {
        if (genres == null) {
            return null;
        }
        String[] tokens = genres.split("[|,]");
        return tokens.length == 0 ? genres : tokens[0].trim();
    }
}
