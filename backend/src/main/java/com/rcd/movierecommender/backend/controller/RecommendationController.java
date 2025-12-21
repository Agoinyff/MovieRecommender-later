package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationRequest;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.service.RecommendationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Validated
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
    @GetMapping
    public List<RecommendationDto> recommend(
            @Valid RecommendationRequest request) {
        RecommendationStrategy strategy = RecommendationStrategy.fromValue(request.getStrategy());
        int size = request.getSize() == null ? 10 : request.getSize();
        return recommendationService.recommend(request.getUserId(), size, strategy);
    }
}
