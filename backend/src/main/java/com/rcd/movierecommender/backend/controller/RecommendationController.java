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

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationDto> recommend(
            @Valid RecommendationRequest request) {
        RecommendationStrategy strategy = RecommendationStrategy.fromValue(request.getStrategy());
        int size = request.getSize() == null ? 10 : request.getSize();
        return recommendationService.recommend(request.getUserId(), size, strategy);
    }
}
