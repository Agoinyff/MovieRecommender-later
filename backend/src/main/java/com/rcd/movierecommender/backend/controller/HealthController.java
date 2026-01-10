package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.service.DataModelWarmer;
import com.rcd.movierecommender.backend.service.FastDataModelBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查和诊断接口
 * 用于监控系统状态、数据模型预热情况、缓存状态等
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "系统健康检查", description = "用于诊断推荐系统的健康状态、数据加载情况、缓存状态等")
public class HealthController {

    private final DataModelWarmer warmer;
    private final CacheManager cacheManager;
    private final FastDataModelBuilder fastBuilder;

    public HealthController(DataModelWarmer warmer, 
                           CacheManager cacheManager,
                           FastDataModelBuilder fastBuilder) {
        this.warmer = warmer;
        this.cacheManager = cacheManager;
        this.fastBuilder = fastBuilder;
    }

    /**
     * 检查数据模型预热状态
     */
    @GetMapping("/warmup-status")
    @Operation(
        summary = "查看数据模型预热状态",
        description = "检查推荐系统的数据模型是否已完成预热加载。" +
                     "预热成功后，warmedUp 为 true，并显示用户数和电影数。" +
                     "如果预热未完成或失败，推荐接口可能较慢或返回空结果。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功获取预热状态",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "预热成功示例",
                    value = "{\n" +
                           "  \"warmedUp\": true,\n" +
                           "  \"status\": \"已预热完成\",\n" +
                           "  \"numUsers\": 162542,\n" +
                           "  \"numItems\": 15234,\n" +
                           "  \"cacheStatus\": \"缓存命中\"\n" +
                           "}"
                )
            )
        )
    })
    public Map<String, Object> checkWarmupStatus() {
        Map<String, Object> result = new HashMap<>();
        
        boolean isWarmedUp = warmer.isWarmedUp();
        result.put("warmedUp", isWarmedUp);
        result.put("status", isWarmedUp ? "已预热完成" : "预热未完成或失败");
        
        // 检查缓存
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get("global");
            if (wrapper != null && wrapper.get() != null) {
                DataModel dataModel = (DataModel) wrapper.get();
                try {
                    result.put("numUsers", dataModel.getNumUsers());
                    result.put("numItems", dataModel.getNumItems());
                    result.put("cacheStatus", "缓存命中");
                } catch (Exception e) {
                    result.put("error", e.getMessage());
                }
            } else {
                result.put("cacheStatus", "缓存为空");
            }
        } else {
            result.put("cacheStatus", "缓存未配置");
        }
        
        return result;
    }

    /**
     * 查询评分总数
     */
    @GetMapping("/rating-count")
    @Operation(
        summary = "查询数据库评分总数",
        description = "直接查询数据库中 movie_preferences 表的总记录数。" +
                     "用于了解数据规模，帮助判断采样率是否合理。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功获取评分总数",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "评分总数示例",
                    value = "{\n" +
                           "  \"totalRatings\": 32000204,\n" +
                           "  \"status\": \"success\"\n" +
                           "}"
                )
            )
        )
    })
    public Map<String, Object> getRatingCount() {
        Map<String, Object> result = new HashMap<>();
        
        long count = fastBuilder.countRatings();
        result.put("totalRatings", count);
        result.put("status", "success");
        
        return result;
    }

    /**
     * 检查特定用户是否在数据模型中
     */
    @GetMapping("/check-user")
    @Operation(
        summary = "检查用户是否存在于数据模型",
        description = "检查指定用户ID是否在已加载的数据模型中。" +
                     "如果用户存在，会显示该用户的评分数量。" +
                     "如果用户不存在，可能是因为：\n" +
                     "1. 该用户没有评分记录\n" +
                     "2. 使用了采样加载，该用户不在采样范围内\n" +
                     "3. 数据模型尚未加载完成\n\n" +
                     "用于诊断推荐接口返回空结果的问题。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功检查用户状态",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "用户存在示例",
                        value = "{\n" +
                               "  \"userId\": 1,\n" +
                               "  \"exists\": true,\n" +
                               "  \"status\": \"用户存在于数据模型中\",\n" +
                               "  \"ratingsCount\": 45\n" +
                               "}"
                    ),
                    @ExampleObject(
                        name = "用户不存在示例",
                        value = "{\n" +
                               "  \"userId\": 1,\n" +
                               "  \"exists\": false,\n" +
                               "  \"status\": \"用户不在数据模型中\",\n" +
                               "  \"reason\": \"该用户可能没有评分记录，或不在采样数据范围内\"\n" +
                               "}"
                    )
                }
            )
        )
    })
    public Map<String, Object> checkUser(
        @Parameter(
            description = "要检查的用户ID",
            example = "1",
            required = true
        )
        @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get("global");
            if (wrapper != null && wrapper.get() != null) {
                DataModel dataModel = (DataModel) wrapper.get();
                try {
                    dataModel.getPreferencesFromUser(userId);
                    result.put("exists", true);
                    result.put("status", "用户存在于数据模型中");
                    result.put("ratingsCount", dataModel.getPreferencesFromUser(userId).length());
                } catch (Exception e) {
                    result.put("exists", false);
                    result.put("status", "用户不在数据模型中");
                    result.put("reason", "该用户可能没有评分记录，或不在采样数据范围内");
                }
            } else {
                result.put("exists", false);
                result.put("status", "数据模型未加载");
            }
        } else {
            result.put("exists", false);
            result.put("status", "缓存未配置");
        }
        
        return result;
    }

    /**
     * 获取所有缓存统计信息
     */
    @GetMapping("/cache-stats")
    @Operation(
        summary = "查看缓存统计信息",
        description = "查看系统中所有缓存的配置和状态。包括：\n" +
                     "- **dataModel**: 数据模型缓存（最重要，影响首次加载速度）\n" +
                     "- **recommendations**: 推荐结果缓存（影响后续请求速度）\n" +
                     "- **movies**: 电影信息缓存\n\n" +
                     "如果缓存未命中，推荐请求会触发重新计算，导致响应变慢。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功获取缓存统计",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "缓存统计示例",
                    value = "{\n" +
                           "  \"dataModel\": \"已缓存\",\n" +
                           "  \"recommendations\": \"已配置\",\n" +
                           "  \"movies\": \"已配置\"\n" +
                           "}"
                )
            )
        )
    })
    public Map<String, Object> getCacheStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查 dataModel 缓存
        Cache dataModelCache = cacheManager.getCache("dataModel");
        if (dataModelCache != null) {
            Cache.ValueWrapper wrapper = dataModelCache.get("global");
            result.put("dataModel", wrapper != null && wrapper.get() != null ? "已缓存" : "未缓存");
        } else {
            result.put("dataModel", "缓存未配置");
        }
        
        // 检查 recommendations 缓存
        Cache recommendationsCache = cacheManager.getCache("recommendations");
        result.put("recommendations", recommendationsCache != null ? "已配置" : "未配置");
        
        // 检查 movies 缓存
        Cache moviesCache = cacheManager.getCache("movies");
        result.put("movies", moviesCache != null ? "已配置" : "未配置");
        
        return result;
    }
}

