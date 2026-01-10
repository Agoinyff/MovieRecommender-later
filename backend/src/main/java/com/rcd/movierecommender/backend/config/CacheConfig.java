package com.rcd.movierecommender.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 使用 Caffeine 作为本地缓存实现，提升推荐系统性能
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存管理器
     * - dataModel: 缓存评分数据模型，1小时过期
     * - recommendations: 缓存推荐结果，30分钟过期
     * - movies: 缓存电影信息，2小时过期
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "dataModel", "recommendations", "movies"
        );
        
        // 数据模型缓存：1小时过期，最多100个条目
        cacheManager.registerCustomCache("dataModel",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(100)
                        .build());
        
        // 推荐结果缓存：30分钟过期，最多1000个条目
        cacheManager.registerCustomCache("recommendations",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .build());
        
        // 电影信息缓存：2小时过期，最多5000个条目
        cacheManager.registerCustomCache("movies",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.HOURS)
                        .maximumSize(5000)
                        .build());
        
        return cacheManager;
    }
}

