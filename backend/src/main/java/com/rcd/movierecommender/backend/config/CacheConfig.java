package com.rcd.movierecommender.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 
 * 使用 Caffeine 作为本地缓存实现，配置三层缓存：
 * 1. dataModel - 数据模型缓存（全局共享，1小时过期）
 * 2. recommendations - 推荐结果缓存（按用户缓存，30分钟过期）
 * 3. movies - 电影信息缓存（2小时过期）
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                // 数据模型缓存：全局共享，1小时过期，最多100条
                buildCache("dataModel", 100, 60),

                // 推荐结果缓存：按用户缓存，30分钟过期，最多1000条
                buildCache("recommendations", 1000, 30),

                // 电影信息缓存：2小时过期，最多5000条
                buildCache("movies", 5000, 120)));
        return cacheManager;
    }

    /**
     * 构建 Caffeine 缓存
     * 
     * @param name          缓存名称
     * @param maxSize       最大缓存条目数
     * @param expireMinutes 过期时间（分钟）
     * @return CaffeineCache 实例
     */
    private CaffeineCache buildCache(String name, int maxSize, int expireMinutes) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(expireMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats() // 启用统计信息，用于监控缓存命中率
                .build());
    }
}
