package com.rcd.movierecommender.backend.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.rcd.movierecommender.backend.auth.RequireAuth;
import com.rcd.movierecommender.backend.auth.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 性能指标监控接口。
 * 提供缓存统计、内存使用等性能监控数据查询，仅管理员可访问。
 */
@RestController
@RequestMapping("/api/metrics")
@RequireAuth(roles = { UserRole.ADMIN })
@Tag(name = "Metrics", description = "性能指标监控接口")
public class MetricsController {

    private final CacheManager cacheManager;

    /**
     * 构造函数注入缓存管理器。
     */
    public MetricsController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 获取缓存统计信息。
     *
     * @return 各缓存区域的命中率、大小等统计数据。
     */
    @GetMapping("/cache")
    @Operation(summary = "获取缓存统计", description = "返回所有缓存区域的命中率、命中次数、未命中次数、当前大小等统计信息。")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        for (String cacheName : Arrays.asList("dataModel", "recommendations", "movies")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats cacheStats = nativeCache.stats();

                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("hitRate", cacheStats.hitRate());
                cacheInfo.put("hitCount", cacheStats.hitCount());
                cacheInfo.put("missCount", cacheStats.missCount());
                cacheInfo.put("loadSuccessCount", cacheStats.loadSuccessCount());
                cacheInfo.put("loadFailureCount", cacheStats.loadFailureCount());
                cacheInfo.put("evictionCount", cacheStats.evictionCount());
                cacheInfo.put("size", nativeCache.estimatedSize());
                stats.put(cacheName, cacheInfo);
            }
        }

        return stats;
    }

    /**
     * 获取内存使用统计。
     *
     * @return JVM 内存使用情况。
     */
    @GetMapping("/memory")
    @Operation(summary = "获取内存统计", description = "返回当前 JVM 的总内存、已用内存、空闲内存、最大内存等信息。")
    public Map<String, Long> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memoryStats = new HashMap<>();
        memoryStats.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memoryStats.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memoryStats.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memoryStats.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        return memoryStats;
    }

    /**
     * 清除所有缓存。
     *
     * @return 清除结果。
     */
    @GetMapping("/cache/clear")
    @Operation(summary = "清除所有缓存", description = "清空 dataModel、recommendations、movies 三个缓存区域的数据，慎用。")
    public Map<String, String> clearAllCaches() {
        for (String cacheName : Arrays.asList("dataModel", "recommendations", "movies")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "所有缓存已清除");
        return result;
    }
}