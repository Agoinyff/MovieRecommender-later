package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import com.rcd.movierecommender.backend.mapper.RatingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评分服务
 * 
 * 提供用户评分的提交、查询以及缓存管理功能
 */
@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 保存或更新用户评分
     * 保存后清除相关缓存，确保推荐结果实时更新
     * 
     * @param userId  用户 ID
     * @param movieId 电影 ID
     * @param rating  评分（1-5）
     */
    @Transactional
    public void saveRating(Long userId, Long movieId, Double rating) {
        try {
            long timestamp = System.currentTimeMillis();
            ratingMapper.insertOrUpdate(userId, movieId, rating, timestamp);
            log.info("用户 {} 对电影 {} 的评分已保存: {}", userId, movieId, rating);

            // 清除缓存，触发数据模型重建
            clearDataModelCache();
            clearUserRecommendationCache(userId);
        } catch (Exception e) {
            log.error("保存评分失败", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "保存评分失败", e);
        }
    }

    /**
     * 查询指定用户的所有评分记录
     * 
     * @param userId 用户 ID
     * @return 评分列表
     */
    public List<RatingDto> getUserRatings(Long userId) {
        try {
            List<RatingEntity> ratings = ratingMapper.findByUserId(userId);

            // 转换为 DTO，包含电影名称
            return ratings.stream()
                    .map(rating -> {
                        MovieEntity movie = movieMapper.findById(rating.getMovieId());
                        return new RatingDto(
                                rating.getUserId(),
                                rating.getMovieId(),
                                movie != null ? movie.getName() : "未知电影",
                                rating.getPreference(),
                                rating.getTimestamp());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询用户评分失败: userId={}", userId, e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "查询用户评分失败", e);
        }
    }

    /**
     * 清除数据模型缓存
     * 当有新评分时，需要重新构建数据模型
     */
    private void clearDataModelCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            cache.clear();
            log.info("数据模型缓存已清除");
        }
    }

    /**
     * 清除指定用户的推荐结果缓存
     * 
     * @param userId 用户 ID
     */
    private void clearUserRecommendationCache(Long userId) {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            // 遍历所有可能的缓存键并清除
            // 格式: userId_size_strategy
            cache.clear(); // 简单起见，清空整个缓存
            log.info("推荐结果缓存已清除: userId={}", userId);
        }
    }
}
