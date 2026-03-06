package com.rcd.movierecommender.backend.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.rcd.movierecommender.backend.auth.RequireRole;
import com.rcd.movierecommender.backend.dto.UserRole;
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

@RestController
@RequestMapping("/api/metrics")
@RequireRole(UserRole.ADMIN)
@Tag(name = "Metrics", description = "性能指标监控接口")
public class MetricsController {

    private final CacheManager cacheManager;

    public MetricsController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/cache")
    @Operation(summary = "获取缓存统计")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<String, Object>();
        for (String cacheName : Arrays.asList("dataModel", "recommendations", "movies")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats cacheStats = nativeCache.stats();
                Map<String, Object> cacheInfo = new HashMap<String, Object>();
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

    @GetMapping("/memory")
    @Operation(summary = "获取内存统计")
    public Map<String, Long> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memoryStats = new HashMap<String, Long>();
        memoryStats.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memoryStats.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memoryStats.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memoryStats.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        return memoryStats;
    }

    @GetMapping("/cache/clear")
    @Operation(summary = "清除所有缓存")
    public Map<String, String> clearAllCaches() {
        for (String cacheName : Arrays.asList("dataModel", "recommendations", "movies")) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("status", "success");
        result.put("message", "所有缓存已清除");
        return result;
    }
}
