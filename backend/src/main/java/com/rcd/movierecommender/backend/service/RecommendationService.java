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
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    
    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;

    /**
     * 推荐服务核心：围绕用户评分构建三种策略（用户协同、物品协同、Slope One）。
     * 所有方法均在只读事务下执行，避免误写数据库。
     */
    public RecommendationService(RatingMapper ratingMapper,
                                 MovieMapper movieMapper) {
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
    }

    /**
     * 为指定用户生成推荐结果。
     *
     * <p>流程：</p>
     * <ol>
     *     <li>加载用户-电影评分矩阵。</li>
     *     <li>根据策略选择对应的打分算法，得到候选电影的预测得分。</li>
     *     <li>按得分降序截断 size 条，并批量查询电影元数据。</li>
     * </ol>
     *
     * @param userId   目标用户 ID。
     * @param size     返回条数。
     * @param strategy 推荐策略（用户、物品、Slope One）。
     * @return 推荐结果列表。
     */
    public List<RecommendationDto> recommend(Long userId, int size, RecommendationStrategy strategy) {
        log.info("收到推荐请求: userId={}, size={}, strategy={}", userId, size, strategy);
        
        // 直接构建 DataModel（不做缓存、采样或预热，保持最小可用实现）
        DataModel dataModel = buildDataModel();
        
        // 打印数据模型统计信息
        try {
            int numUsers = dataModel.getNumUsers();
            int numItems = dataModel.getNumItems();
            log.info("数据模型统计: {} 个用户, {} 个电影", numUsers, numItems);
        } catch (TasteException e) {
            log.error("获取数据模型统计失败", e);
        }
        
        if (!userExists(userId, dataModel)) {
            log.warn("用户 {} 不在数据模型中，返回空推荐列表", userId);
            log.warn("提示：该用户可能没有评分记录，或不在采样数据范围内");
            return Collections.emptyList();
        }
        
        log.info("用户 {} 存在于数据模型中，开始生成推荐...", userId);

        try {
            List<RecommendedItem> recommendedItems = buildRecommender(dataModel, strategy)
                    .recommend(userId, size);
            
            // 批量查询电影信息，避免 N+1 查询问题
            return batchToRecommendationDtos(recommendedItems);
        } catch (TasteException e) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_ENGINE_ERROR,
                    "Mahout 推荐算法计算失败，无法生成推荐结果，原因：" + e.getMessage(), e, buildRootCause(e));
        }
    }

    /**
     * 构建数据模型（直接加载全部评分数据，流程更直观）
     */
    public DataModel buildDataModel() {
        try {
            log.info("开始构建数据模型...");
            long startTime = System.currentTimeMillis();
            
            // 用户 -> 评分列表
            FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<>();
            Map<Long, List<Preference>> preferences = new HashMap<>();

            // 一次性加载全部评分（论文场景更容易说明）
            List<RatingEntity> ratings = ratingMapper.findAllRatings();
            for (RatingEntity rating : ratings) {
                preferences
                        .computeIfAbsent(rating.getUserId(), id -> new ArrayList<>())
                        .add(new GenericPreference(
                                rating.getUserId(),
                                rating.getMovieId(),
                                rating.getPreference().floatValue()
                        ));
            }

            log.info("评分数据加载完成，共 {} 条，涉及 {} 个用户", ratings.size(), preferences.size());
            
            // 构建用户偏好数组
            for (Map.Entry<Long, List<Preference>> entry : preferences.entrySet()) {
                preferenceMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
            }
            
            preferences.clear();
            
            long endTime = System.currentTimeMillis();
            log.info("数据模型构建完成，耗时: {} 秒", (endTime - startTime) / 1000.0);
            
            return new GenericDataModel(preferenceMap);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "加载评分数据失败，无法构建推荐所需的数据模型", e, buildRootCause(e));
        }
    }

    private boolean userExists(Long userId, DataModel dataModel) {
        try {
            PreferenceArray prefs = dataModel.getPreferencesFromUser(userId);
            log.debug("用户 {} 有 {} 条评分记录", userId, prefs.length());
            return true;
        } catch (TasteException e) {
            log.debug("用户 {} 不存在: {}", userId, e.getMessage());
            return false;
        }
    }

    private Recommender buildRecommender(DataModel dataModel, RecommendationStrategy strategy) throws TasteException {
        switch (strategy) {
            case ITEM_BASED:
                ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
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

    /**
     * 批量转换推荐结果为 DTO（优化：一次性批量查询电影信息）
     */
    private List<RecommendationDto> batchToRecommendationDtos(List<RecommendedItem> recommendedItems) {
        if (recommendedItems.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 收集所有需要查询的电影 ID
            List<Long> movieIds = recommendedItems.stream()
                    .map(RecommendedItem::getItemID)
                    .collect(Collectors.toList());

            // 批量查询电影信息
            List<MovieEntity> movies = movieIds.isEmpty()
                    ? Collections.emptyList()
                    : movieMapper.findByIds(movieIds);
            Map<Long, MovieEntity> movieMap = movies.stream()
                    .collect(Collectors.toMap(MovieEntity::getId, m -> m));

            // 构建推荐结果列表
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
                                item.getValue()
                        );
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "批量查询电影基础信息失败，无法返回完整的推荐结果", e, buildRootCause(e));
        }
    }

    /**
     * 单个电影信息转换（保留用于其他场景，但推荐接口已改用批量查询）
     */
    @Deprecated
    private RecommendationDto toRecommendationDto(Long movieId, double score) {
        try {
            MovieEntity movie = movieMapper.findById(movieId);
            if (movie == null) {
                return null;
            }
            return new RecommendationDto(movie.getId(), movie.getName(), movie.getPublishedYear(), movie.getGenres(), score);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "查询电影基础信息失败，无法返回完整的推荐结果", e, buildRootCause(e));
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
