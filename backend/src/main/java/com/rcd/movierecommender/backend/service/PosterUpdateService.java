package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 海报更新服务，用于批量更新电影海报 URL
 */
@Service
public class PosterUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PosterUpdateService.class);

    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;

    public PosterUpdateService(MovieMapper movieMapper, TmdbService tmdbService) {
        this.movieMapper = movieMapper;
        this.tmdbService = tmdbService;
    }

    /**
     * 异步批量更新电影海报
     * 只更新没有海报的电影
     */
    @Async
    public void updateMissingPosters() {
        log.info("Starting batch poster update for movies without posters...");

        try {
            // 获取所有没有海报的电影（分批处理）
            int page = 0;
            int pageSize = 50;
            int totalUpdated = 0;

            while (true) {
                int offset = page * pageSize;
                List<MovieEntity> movies = movieMapper.findMovies(null, pageSize, offset);

                if (movies.isEmpty()) {
                    break;
                }

                for (MovieEntity movie : movies) {
                    // 只处理没有海报 URL 的电影
                    if (movie.getPosterUrl() == null || movie.getPosterUrl().trim().isEmpty()) {
                        String posterUrl = tmdbService.searchMoviePoster(movie.getName(), movie.getPublishedYear());

                        if (posterUrl != null) {
                            // 更新数据库
                            movieMapper.updatePosterUrl(movie.getId(), posterUrl);
                            totalUpdated++;
                            log.debug("Updated poster for movie: {} (ID: {})", movie.getName(), movie.getId());
                        }

                        // 添加延迟避免 API 限流
                        try {
                            Thread.sleep(250); // 每秒最多 4 个请求
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.warn("Poster update interrupted", e);
                            break;
                        }
                    }
                }

                page++;
            }

            log.info("Batch poster update completed. Updated {} movies.", totalUpdated);

        } catch (Exception e) {
            log.error("Error during batch poster update", e);
        }
    }

    /**
     * 为单个电影更新海报（异步）
     */
    @Async
    public void updatePosterForMovie(Long movieId, String movieName, String year) {
        try {
            String posterUrl = tmdbService.searchMoviePoster(movieName, year);
            if (posterUrl != null) {
                movieMapper.updatePosterUrl(movieId, posterUrl);
                log.info("Updated poster for movie ID: {}", movieId);
            }
        } catch (Exception e) {
            log.error("Error updating poster for movie ID: {}", movieId, e);
        }
    }

    /**
     * 快速更新前 N 部电影的海报（用于快速预览效果）
     */
    @Async
    public void updateFirstNMovies(int count) {
        log.info("Starting fast poster update for first {} movies...", count);

        try {
            List<MovieEntity> movies = movieMapper.findMovies(null, count, 0);
            int totalUpdated = 0;

            for (MovieEntity movie : movies) {
                // 只处理没有海报 URL 的电影
                if (movie.getPosterUrl() == null || movie.getPosterUrl().trim().isEmpty()) {
                    String posterUrl = tmdbService.searchMoviePoster(movie.getName(), movie.getPublishedYear());

                    if (posterUrl != null) {
                        movieMapper.updatePosterUrl(movie.getId(), posterUrl);
                        totalUpdated++;
                        log.info("Updated poster for movie: {} (ID: {})", movie.getName(), movie.getId());
                    }

                    // 添加延迟避免 API 限流
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Poster update interrupted", e);
                        break;
                    }
                }
            }

            log.info("Fast poster update completed. Updated {} out of {} movies.", totalUpdated, count);

        } catch (Exception e) {
            log.error("Error during fast poster update", e);
        }
    }
}
