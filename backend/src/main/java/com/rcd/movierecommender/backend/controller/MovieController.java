package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    /**
     * 构造函数注入服务层，便于测试与解耦。
     */
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * 按关键字分页检索电影。
     *
     * @param query 模糊匹配的名称关键字，允许为空以返回全量列表。
     * @param page 页码，从 0 开始。
     * @param size 每页数量，默认 20。
     * @return 分页的电影结果。
     */
    @GetMapping
    @Operation(summary = "分页检索电影", description = "按名称关键字模糊搜索电影，不传 query 返回全量分页。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content)
    })
    public Page<MovieDto> search(
            @Parameter(description = "模糊匹配的电影名称关键字，可为空")
            @RequestParam(value = "query", required = false) String query,
            @Parameter(description = "页码，从 0 开始", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "每页条数", example = "20")
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
    @Operation(summary = "获取电影详情", description = "根据电影 ID 返回单条电影信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = MovieDto.class))),
            @ApiResponse(responseCode = "404", description = "电影不存在", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content)
    })
    public ResponseEntity<MovieDto> getMovie(
            @Parameter(description = "电影主键", example = "1")
            @PathVariable Long id) {
        return movieService.getMovie(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 获取电影详情页底部的联动推荐列表。
     *
     * @param id 电影主键。
     * @param size 返回条数。
     * @return 同类型或相近题材的电影列表。
     */
    @GetMapping("/{id}/related")
    @Operation(summary = "获取关联电影", description = "根据当前电影的类型和题材生成详情页底部的横向推荐列表。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MovieDto.class)))),
            @ApiResponse(responseCode = "404", description = "电影不存在", content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content)
    })
    public List<MovieDto> getRelatedMovies(
            @Parameter(description = "电影主键", example = "1")
            @PathVariable Long id,
            @Parameter(description = "返回条数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return movieService.getRelatedMovies(id, size);
    }
}