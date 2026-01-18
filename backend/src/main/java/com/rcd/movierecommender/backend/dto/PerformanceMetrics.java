package com.rcd.movierecommender.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 性能指标 DTO
 * 
 * 用于记录和返回推荐请求的性能指标信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 推荐策略
     */
    private RecommendationStrategy strategy;

    /**
     * 请求的推荐数量
     */
    private Integer requestedSize;

    /**
     * 实际返回的推荐数量
     */
    private Integer actualSize;

    /**
     * 请求耗时（毫秒）
     */
    private Long elapsedTimeMs;

    /**
     * 内存增量（MB）
     */
    private Long memoryUsedMb;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 是否命中缓存
     */
    private Boolean cacheHit;
}
