package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContextHolder;
import com.rcd.movierecommender.backend.auth.RequireLogin;
import com.rcd.movierecommender.backend.dto.PagedRatingResponse;
import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.dto.RatingRequest;
import com.rcd.movierecommender.backend.dto.RatingStatsDto;
import com.rcd.movierecommender.backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ratings")
@Validated
@Tag(name = "Rating", description = "用户评分接口")
@RequireLogin
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @Operation(summary = "提交评分")
    public ResponseEntity<String> submitRating(@Valid @RequestBody RatingRequest request) {
        ratingService.saveRating(AuthContextHolder.getUserId(), request.getMovieId(), request.getRating());
        return ResponseEntity.ok("评分提交成功");
    }

    @GetMapping("/me")
    @Operation(summary = "我的评分列表")
    public PagedRatingResponse getMyRatings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return ratingService.getPagedRatings(AuthContextHolder.getUserId(), query, minRating, maxRating, page, size);
    }

    @GetMapping("/me/stats")
    @Operation(summary = "我的评分统计")
    public RatingStatsDto getMyRatingStats() {
        return ratingService.getRatingStats(AuthContextHolder.getUserId());
    }

    @GetMapping("/me/movies/{movieId}")
    @Operation(summary = "我对某部电影的评分")
    public ResponseEntity<RatingDto> getMyMovieRating(@PathVariable Long movieId) {
        RatingDto rating = ratingService.getUserRating(AuthContextHolder.getUserId(), movieId);
        return rating == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(rating);
    }
}
