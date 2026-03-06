package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.PagedRatingResponse;
import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.dto.RatingStatsDto;
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
import java.util.stream.Collectors;

@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;
    private final CacheManager cacheManager;
    private final ModelWarmupService modelWarmupService;

    public RatingService(RatingMapper ratingMapper, MovieMapper movieMapper, CacheManager cacheManager,
            ModelWarmupService modelWarmupService) {
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
        this.cacheManager = cacheManager;
        this.modelWarmupService = modelWarmupService;
    }

    @Transactional
    public void saveRating(Long userId, Long movieId, Double rating) {
        try {
            MovieEntity movie = movieMapper.findById(movieId);
            if (movie == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "电影不存在");
            }
            long timestamp = System.currentTimeMillis() / 1000;
            ratingMapper.insertOrUpdate(userId, movieId, rating, timestamp);
            clearDataModelCache();
            clearRecommendationCache();
            modelWarmupService.warmupDataModel();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("保存评分失败", ex);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "保存评分失败", ex);
        }
    }

    public List<RatingDto> getUserRatings(Long userId) {
        return ratingMapper.findByUserId(userId).stream().map(new java.util.function.Function<RatingEntity, RatingDto>() {
            @Override
            public RatingDto apply(RatingEntity rating) {
                MovieEntity movie = movieMapper.findById(rating.getMovieId());
                return new RatingDto(rating.getUserId(), rating.getMovieId(), movie == null ? "未知电影" : movie.getName(),
                        rating.getPreference(), rating.getTimestamp());
            }
        }).collect(Collectors.toList());
    }

    public PagedRatingResponse getPagedRatings(Long userId, String query, Double minRating, Double maxRating, int page,
            int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        List<RatingDto> content = ratingMapper.findFilteredUserRatings(userId, normalize(query), minRating, maxRating,
                safeSize, safePage * safeSize);
        long total = ratingMapper.countFilteredUserRatings(userId, normalize(query), minRating, maxRating);
        int totalPages = (int) Math.ceil(total / (double) safeSize);
        return new PagedRatingResponse(content, total, totalPages, safePage, safeSize);
    }

    public RatingDto getUserRating(Long userId, Long movieId) {
        RatingDto rating = ratingMapper.findUserMovieRating(userId, movieId);
        if (rating == null) {
            return null;
        }
        MovieEntity movie = movieMapper.findById(movieId);
        rating.setMovieName(movie == null ? "未知电影" : movie.getName());
        return rating;
    }

    public RatingStatsDto getRatingStats(Long userId) {
        return new RatingStatsDto(
                ratingMapper.countByUserId(userId),
                safeDouble(ratingMapper.getAverageRatingByUserId(userId)),
                ratingMapper.countHighRatingsByUserId(userId),
                ratingMapper.getRecentTimestampByUserId(userId));
    }

    public boolean hasEnoughRatings(Long userId, long minRatings) {
        return ratingMapper.countByUserId(userId) >= minRatings;
    }

    private void clearDataModelCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            cache.clear();
        }
    }

    private void clearRecommendationCache() {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            cache.clear();
        }
    }

    private String normalize(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
