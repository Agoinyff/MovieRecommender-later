package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationRequest;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
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
     * 根据指定策略为用户生成推荐列表。
     * 使用 {@link RecommendationRequest} 进行参数校验，保证 userId 与 strategy 必填、size 在 1-50 之间。
     *
     * @param request 查询参数封装。
     * @return 推荐结果列表，按得分从高到低排序。
     */
    @Operation(summary = "获取推荐列表", description = "根据用户 ID 与推荐策略生成 Top N 推荐列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "推荐成功",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RecommendationDto.class)))),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content)
    })
    @GetMapping
    public List<RecommendationDto> recommend(
            @Parameter(description = "请求参数，包括 userId、size、strategy")
            @Valid RecommendationRequest request) {
        RecommendationStrategy strategy = RecommendationStrategy.fromValue(request.getStrategy());
        int size = request.getSize() == null ? 10 : request.getSize();
        return recommendationService.recommend(request.getUserId(), size, strategy);
    }
}
