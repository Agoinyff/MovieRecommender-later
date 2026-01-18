package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.dto.RatingRequest;
import com.rcd.movierecommender.backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评分管理接口
 * 
 * 提供用户评分的提交和查询功能
 */
@RestController
@RequestMapping("/api/ratings")
@Validated
@Tag(name = "Rating", description = "用户评分管理接口")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    /**
     * 提交用户评分
     * 
     * @param request 评分请求
     * @return 成功响应
     */
    @PostMapping
    @Operation(summary = "提交评分", description = "用户对电影进行评分，评分范围为 1-5")
    public ResponseEntity<String> submitRating(
            @Parameter(description = "评分请求，包含 userId、movieId 和 rating") @Valid @RequestBody RatingRequest request) {
        ratingService.saveRating(request.getUserId(), request.getMovieId(), request.getRating());
        return ResponseEntity.ok("评分提交成功");
    }

    /**
     * 查询用户的所有评分
     * 
     * @param userId 用户 ID
     * @return 评分列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户评分", description = "获取指定用户的所有评分记录")
    public List<RatingDto> getUserRatings(
            @Parameter(description = "用户 ID") @PathVariable Long userId) {
        return ratingService.getUserRatings(userId);
    }
}
