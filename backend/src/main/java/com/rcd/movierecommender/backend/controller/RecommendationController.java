package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContext;
import com.rcd.movierecommender.backend.auth.RequireAuth;
import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationRequest;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Validated
@Tag(name = "Recommendation", description = "基于策略的电影推荐接口")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 构造函数注入推荐服务。
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * 获取推荐列表。
     * 普通用户默认围绕当前登录用户生成推荐；管理员可以额外传入 userId 查询指定用户的推荐结果。
     *
     * @param request 推荐请求参数。
     * @return 推荐结果列表，按得分从高到低排序。
     */
    @RequireAuth
    @GetMapping
    @Operation(summary = "获取推荐列表", description = "普通用户获取当前登录用户的推荐结果；管理员可按用户 ID 查询指定用户的推荐结果。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "推荐成功",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RecommendationDto.class)))),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content),
            @ApiResponse(responseCode = "403", description = "无权限按用户 ID 查询推荐", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content)
    })
    public List<RecommendationDto> recommend(
            @Parameter(description = "请求参数，包括目标用户 ID、返回条数和推荐策略")
            @Valid RecommendationRequest request) {
        Long currentUserId = AuthContext.requireCurrentUser().getId();
        Long targetUserId = currentUserId;
        if (request.getUserId() != null) {
            if (!AuthContext.requireCurrentUser().isAdmin()) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可以按用户 ID 查询推荐结果");
            }
            targetUserId = request.getUserId();
        }
        RecommendationStrategy strategy = RecommendationStrategy.fromValue(request.getStrategy());
        int size = request.getSize() == null ? 10 : request.getSize();
        return recommendationService.recommend(targetUserId, size, strategy);
    }
}