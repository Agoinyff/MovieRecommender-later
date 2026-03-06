package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContextHolder;
import com.rcd.movierecommender.backend.auth.RequireLogin;
import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.dto.MovieRecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.service.MovieService;
import com.rcd.movierecommender.backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movie", description = "电影检索与详情接口")
public class MovieController {

    private final MovieService movieService;
    private final RecommendationService recommendationService;

    public MovieController(MovieService movieService, RecommendationService recommendationService) {
        this.movieService = movieService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(summary = "分页搜索电影")
    public Page<MovieDto> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return movieService.searchMovies(query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取电影详情")
    public ResponseEntity<MovieDto> getMovie(@PathVariable Long id) {
        return movieService.getMovie(id)
                .map(ResponseEntity::ok)
                .orElseGet(new java.util.function.Supplier<ResponseEntity<MovieDto>>() {
                    @Override
                    public ResponseEntity<MovieDto> get() {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @GetMapping("/popular")
    @Operation(summary = "热门电影")
    public List<MovieDto> getPopularMovies(@RequestParam(value = "size", defaultValue = "10") int size) {
        return movieService.getPopularMovies(size);
    }

    @GetMapping("/{id}/recommendations")
    @RequireLogin
    @Operation(summary = "电影详情页推荐")
    public List<MovieRecommendationDto> getMovieRecommendations(
            @PathVariable Long id,
            @RequestParam(value = "strategy", defaultValue = "ITEM_BASED") String strategy,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return recommendationService.recommendForMovie(AuthContextHolder.getUserId(), id, size,
                RecommendationStrategy.fromValue(strategy));
    }
}
