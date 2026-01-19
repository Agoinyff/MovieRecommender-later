package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.mapper.RatingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查接口
 * 用于确认系统可用性与数据规模
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "系统健康检查", description = "用于诊断推荐系统的健康状态与数据规模")
public class HealthController {

    private final RatingMapper ratingMapper;

    public HealthController(RatingMapper ratingMapper) {
        this.ratingMapper = ratingMapper;
    }

    /**
     * 基础健康检查
     */
    @GetMapping("/status")
    @Operation(summary = "系统状态检查", description = "返回系统运行状态，用于快速检测服务是否可用。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "服务正常", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "状态示例", value = "{\n"
                    +
                    "  \"status\": \"ok\"\n" +
                    "}")))
    })
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }

    /**
     * 查询评分总数
     */
    @GetMapping("/rating-count")
    @Operation(summary = "查询数据库评分总数", description = "直接查询数据库中 movie_preferences 表的总记录数，用于了解数据规模。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取评分总数", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "评分总数示例", value = "{\n"
                    +
                    "  \"totalRatings\": 1000209,\n" +
                    "  \"status\": \"success\"\n" +
                    "}")))
    })
    public Map<String, Object> getRatingCount() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 添加超时保护，避免长时间挂起
            long count = ratingMapper.countRatings();
            result.put("totalRatings", count);
            result.put("status", "success");
        } catch (Exception e) {
            // 如果查询失败，返回错误信息而不是挂起
            result.put("totalRatings", 0);
            result.put("status", "error");
            result.put("message", "查询评分总数失败: " + e.getMessage());
        }

        return result;
    }
}
