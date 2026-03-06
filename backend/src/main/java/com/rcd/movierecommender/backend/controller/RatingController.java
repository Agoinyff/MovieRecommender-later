package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContext;
import com.rcd.movierecommender.backend.auth.RequireAuth;
import com.rcd.movierecommender.backend.auth.UserRole;
import com.rcd.movierecommender.backend.dto.CurrentRatingDto;
import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.dto.RatingRequest;
import com.rcd.movierecommender.backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 评分管理接口。
 * 提供当前登录用户评分的提交与查询能力，并保留管理员按用户 ID 查看评分的能力。
 */
@RestController
@RequestMapping("/api/ratings")
@Validated
@Tag(name = "Rating", description = "用户评分管理接口")
public class RatingController {

    private final RatingService ratingService;

    /**
     * 构造函数注入评分服务。
     */
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * 提交当前登录用户的评分。
     *
     * @param request 评分请求。
     * @return 成功响应。
     */
    @RequireAuth
    @PostMapping
    @Operation(summary = "提交评分", description = "当前登录用户对指定电影进行评分，评分范围为 1-5。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "评分提交成功", content = @Content),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public ResponseEntity<String> submitRating(
            @Parameter(description = "评分请求，包含 movieId 和 rating")
            @Valid @RequestBody RatingRequest request) {
        ratingService.saveRating(AuthContext.requireCurrentUser().getId(), request.getMovieId(), request.getRating());
        return ResponseEntity.ok("评分提交成功");
    }

    /**
     * 查询当前登录用户的全部评分。
     *
     * @return 评分列表。
     */
    @RequireAuth
    @GetMapping("/me")
    @Operation(summary = "查询我的评分", description = "获取当前登录用户的全部评分记录，用于个人中心展示。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RatingDto.class)))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public List<RatingDto> getMyRatings() {
        return ratingService.getUserRatings(AuthContext.requireCurrentUser().getId());
    }

    /**
     * 查询当前登录用户对指定电影的评分。
     *
     * @param movieId 电影 ID。
     * @return 找到则返回评分，未评分返回 204。
     */
    @RequireAuth
    @GetMapping("/me/movie/{movieId}")
    @Operation(summary = "查询我对单部电影的评分", description = "用于电影详情页回显当前登录用户已提交的评分。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = CurrentRatingDto.class))),
            @ApiResponse(responseCode = "204", description = "当前用户尚未评分", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public ResponseEntity<CurrentRatingDto> getMyMovieRating(
            @Parameter(description = "电影 ID", example = "1")
            @PathVariable Long movieId) {
        return ratingService.getCurrentRating(AuthContext.requireCurrentUser().getId(), movieId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * 管理员按用户 ID 查询评分。
     *
     * @param userId 用户 ID。
     * @return 指定用户的评分列表。
     */
    @RequireAuth(roles = { UserRole.ADMIN })
    @GetMapping("/user/{userId}")
    @Operation(summary = "按用户查询评分", description = "管理员按用户 ID 查询指定用户的评分记录。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RatingDto.class)))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content),
            @ApiResponse(responseCode = "403", description = "无管理员权限", content = @Content)
    })
    public List<RatingDto> getUserRatings(
            @Parameter(description = "用户 ID", example = "1000002")
            @PathVariable Long userId) {
        return ratingService.getUserRatings(userId);
    }
}