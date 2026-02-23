package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.service.PosterUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 海报管理控制器
 * 提供手动触发海报更新的接口
 */
@RestController
@RequestMapping("/api/posters")
@CrossOrigin(origins = "*")
public class PosterController {

    private final PosterUpdateService posterUpdateService;

    public PosterController(PosterUpdateService posterUpdateService) {
        this.posterUpdateService = posterUpdateService;
    }

    /**
     * 手动触发批量更新所有缺失的海报
     */
    @PostMapping("/update-all")
    public ResponseEntity<Map<String, Object>> updateAllPosters() {
        // 异步执行，立即返回
        posterUpdateService.updateMissingPosters();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "海报批量更新任务已启动，正在后台执行");
        response.put("status", "started");

        return ResponseEntity.ok(response);
    }

    /**
     * 快速更新前 N 部电影的海报（用于快速预览）
     */
    @PostMapping("/update-first")
    public ResponseEntity<Map<String, Object>> updateFirstPosters(
            @RequestParam(defaultValue = "100") int count) {

        posterUpdateService.updateFirstNMovies(count);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "正在更新前 " + count + " 部电影的海报");
        response.put("count", count);
        response.put("status", "started");

        return ResponseEntity.ok(response);
    }

    /**
     * 为单个电影更新海报
     */
    @PostMapping("/{movieId}")
    public ResponseEntity<Map<String, Object>> updateMoviePoster(
            @PathVariable Long movieId,
            @RequestParam String movieName,
            @RequestParam(required = false) String year) {

        posterUpdateService.updatePosterForMovie(movieId, movieName, year);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "海报更新任务已启动");
        response.put("movieId", movieId);
        response.put("status", "started");

        return ResponseEntity.ok(response);
    }
}
