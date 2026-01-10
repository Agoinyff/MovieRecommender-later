package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.repository.MovieRepository;
import com.rcd.movierecommender.backend.repository.RatingRepository;
import com.rcd.movierecommender.backend.service.recommender.slopeone.CustomSlopeOneRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
//import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final FastDataModelBuilder fastBuilder;
    
    // 每次从数据库加载的批次大小（旧方法使用，新方法使用流式加载）
    private static final int BATCH_SIZE = 5000;
    
    // 数据采样率：1.0=全部数据，0.5=50%，0.1=10%
    // 对于3200万数据，建议0.3-0.5（构建时间从2小时降至10-30分钟）
    private static final double SAMPLE_RATE = 0.5;

    /**
     * 推荐服务核心：围绕用户评分构建三种策略（用户协同、物品协同、Slope One）。
     * 所有方法均在只读事务下执行，避免误写数据库。
     */
    public RecommendationService(RatingRepository ratingRepository, 
                                 MovieRepository movieRepository,
                                 FastDataModelBuilder fastBuilder) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
        this.fastBuilder = fastBuilder;
    }

    /**
     * 为指定用户生成推荐结果。（添加缓存支持）
     *
     * <p>流程：</p>
     * <ol>
     *     <li>从缓存中获取或构建用户-电影评分矩阵。</li>
     *     <li>根据策略选择对应的打分算法，得到候选电影的预测得分。</li>
     *     <li>按得分降序截断 size 条，并批量查询电影元数据。</li>
     * </ol>
     *
     * @param userId   目标用户 ID。
     * @param size     返回条数。
     * @param strategy 推荐策略（用户、物品、Slope One）。
     * @return 推荐结果列表。
     */
    @Cacheable(value = "recommendations", key = "#userId + '_' + #size + '_' + #strategy", unless = "#result.isEmpty()")
    public List<RecommendationDto> recommend(Long userId, int size, RecommendationStrategy strategy) {
        log.info("收到推荐请求: userId={}, size={}, strategy={}", userId, size, strategy);
        
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
     * 构建数据模型（使用快速JDBC流式加载，比JPA快5-10倍）
     * 缓存键：固定为 "global"，因为数据模型对所有用户是共享的
     * 
     * 注意：实际加载由 DataModelWarmer 在应用启动时异步执行
     * 这个方法只在缓存失效时才会被调用
     */
    @Cacheable(value = "dataModel", key = "'global'")
    public DataModel buildDataModel() {
        log.warn("缓存未命中，开始构建数据模型（采样率: {}）...", SAMPLE_RATE);
        log.warn("如果这不是首次启动，说明缓存可能已过期");
        
        try {
            return fastBuilder.buildDataModel(SAMPLE_RATE);
        } catch (Exception e) {
            log.error("使用快速构建器失败，回退到JPA方式", e);
            return buildDataModelWithJPA();
        }
    }
    
    /**
     * 使用JPA分页加载构建数据模型（备用方案，慢但稳定）
     * 仅在快速构建器失败时使用
     */
    private DataModel buildDataModelWithJPA() {
        try {
            log.info("使用JPA方式构建数据模型...");
            long startTime = System.currentTimeMillis();
            
            FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<>();
            Map<Long, List<Preference>> preferences = new HashMap<>();
            
            // 查询总数
            long totalCount = ratingRepository.countAll();
            log.info("评分总数: {}", totalCount);
            
            // 计算需要的分页次数
            int totalPages = (int) Math.ceil((double) totalCount / BATCH_SIZE);
            log.info("将分 {} 批加载数据，每批 {} 条", totalPages, BATCH_SIZE);
            
            // 分页加载数据
            int loadedCount = 0;
            for (int page = 0; page < totalPages; page++) {
                Page<RatingEntity> ratingPage = ratingRepository.findAll(PageRequest.of(page, BATCH_SIZE));
                
                for (RatingEntity rating : ratingPage.getContent()) {
                    preferences
                            .computeIfAbsent(rating.getUserId(), id -> new ArrayList<>())
                            .add(new GenericPreference(
                                    rating.getUserId(), 
                                    rating.getMovieId(), 
                                    rating.getPreference().floatValue()
                            ));
                    loadedCount++;
                }
                
                if ((page + 1) % 5 == 0) {
                    log.info("已加载 {}/{} 条评分数据 ({}/{}页)", loadedCount, totalCount, page + 1, totalPages);
                }
            }
            
            log.info("评分数据加载完成，共 {} 条，涉及 {} 个用户", loadedCount, preferences.size());
            
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
        } catch (OutOfMemoryError e) {
            log.error("内存溢出！请增加 JVM 堆内存大小（-Xmx 参数）");
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "数据量过大导致内存不足，请联系管理员增加服务器内存配置", e, Collections.singletonList("OutOfMemoryError: " + e.getMessage()));
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
            List<MovieEntity> movies = movieRepository.findAllById(movieIds);
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
            Optional<MovieEntity> movieOpt = movieRepository.findById(movieId);
            if (!movieOpt.isPresent()) {
                return null;
            }
            MovieEntity movie = movieOpt.get();
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
