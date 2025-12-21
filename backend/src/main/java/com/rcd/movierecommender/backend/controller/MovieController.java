package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    /**
     * 构造函数注入服务层，便于测试与解耦。
     */
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * 按关键字分页检索电影。
     *
     * @param query  模糊匹配的名称关键字，允许为空以返回全量列表。
     * @param page   页码，从 0 开始。
     * @param size   每页数量，默认 20。
     * @return 分页的电影结果。
     */
    @GetMapping
    public Page<MovieDto> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return movieService.searchMovies(query, page, size);
    }

    /**
     * 根据主键获取单个电影详情。
     *
     * @param id 电影主键。
     * @return 找到时返回 200 + MovieDto，未找到时返回 404。
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovie(@PathVariable Long id) {
        return movieService.getMovie(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
