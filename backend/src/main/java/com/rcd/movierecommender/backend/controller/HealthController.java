package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.RequireRole;
import com.rcd.movierecommender.backend.dto.UserRole;
import com.rcd.movierecommender.backend.mapper.RatingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequireRole(UserRole.ADMIN)
@Tag(name = "Health", description = "系统健康检查")
public class HealthController {

    private final RatingMapper ratingMapper;

    public HealthController(RatingMapper ratingMapper) {
        this.ratingMapper = ratingMapper;
    }

    @GetMapping("/status")
    @Operation(summary = "系统状态")
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "ok");
        return result;
    }

    @GetMapping("/rating-count")
    @Operation(summary = "评分总数")
    public Map<String, Object> getRatingCount() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalRatings", ratingMapper.countRatings());
        result.put("status", "success");
        return result;
    }
}
