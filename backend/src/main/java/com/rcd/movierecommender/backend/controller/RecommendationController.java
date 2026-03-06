package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContextHolder;
import com.rcd.movierecommender.backend.auth.RequireLogin;
import com.rcd.movierecommender.backend.auth.RequireRole;
import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.dto.UserRole;
import com.rcd.movierecommender.backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation", description = "推荐接口")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/me")
    @RequireLogin
    @Operation(summary = "当前用户推荐")
    public List<RecommendationDto> recommendForCurrentUser(
            @RequestParam(value = "strategy") String strategy,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return recommendationService.recommendWithFallback(AuthContextHolder.getUserId(), size,
                RecommendationStrategy.fromValue(strategy));
    }

    @GetMapping("/admin")
    @RequireRole(UserRole.ADMIN)
    @Operation(summary = "管理员按用户 ID 查看推荐")
    public List<RecommendationDto> recommendForAnyUser(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "strategy") String strategy,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return recommendationService.recommendWithFallback(userId, size, RecommendationStrategy.fromValue(strategy));
    }

    @GetMapping("/popular")
    @Operation(summary = "热门电影兜底推荐")
    public List<RecommendationDto> popularRecommendations(
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return recommendationService.getPopularRecommendations(size);
    }
}
