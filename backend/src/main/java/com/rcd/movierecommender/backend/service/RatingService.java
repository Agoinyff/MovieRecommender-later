package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.CurrentRatingDto;
import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import com.rcd.movierecommender.backend.mapper.RatingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;
    private final CacheManager cacheManager;
    private final ModelWarmupService modelWarmupService;

    public RatingService(RatingMapper ratingMapper,
            MovieMapper movieMapper,
            CacheManager cacheManager,
            ModelWarmupService modelWarmupService) {
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
        this.cacheManager = cacheManager;
        this.modelWarmupService = modelWarmupService;
    }

    @Transactional
    public void saveRating(Long userId, Long movieId, Double rating) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            if (ratingMapper.countByUserIdAndMovieId(userId, movieId) > 0) {
                ratingMapper.updateRating(userId, movieId, rating, timestamp);
            } else {
                ratingMapper.insertRating(userId, movieId, rating, timestamp);
            }
            clearDataModelCache();
            clearUserRecommendationCache();
            modelWarmupService.warmupDataModel();
            log.info("Rating saved. userId={}, movieId={}, rating={}", userId, movieId, rating);
        } catch (Exception ex) {
            log.error("Failed to save rating", ex);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "保存评分失败", ex);
        }
    }

    public List<RatingDto> getUserRatings(Long userId) {
        try {
            return ratingMapper.findByUserId(userId).stream()
                    .map(this::toRatingDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Failed to load ratings. userId={}", userId, ex);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询用户评分失败", ex);
        }
    }

    public Optional<CurrentRatingDto> getCurrentRating(Long userId, Long movieId) {
        try {
            RatingEntity rating = ratingMapper.findLatestByUserIdAndMovieId(userId, movieId);
            if (rating == null) {
                return Optional.empty();
            }
            return Optional.of(new CurrentRatingDto(rating.getMovieId(), rating.getPreference(), rating.getTimestamp()));
        } catch (Exception ex) {
            log.error("Failed to load current rating. userId={}, movieId={}", userId, movieId, ex);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询当前评分失败", ex);
        }
    }

    private RatingDto toRatingDto(RatingEntity rating) {
        MovieEntity movie = movieMapper.findById(rating.getMovieId());
        return new RatingDto(
                rating.getUserId(),
                rating.getMovieId(),
                movie != null ? movie.getName() : "Unknown Movie",
                rating.getPreference(),
                rating.getTimestamp());
    }

    private void clearDataModelCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            cache.clear();
        }
    }

    private void clearUserRecommendationCache() {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            cache.clear();
        }
    }
}
