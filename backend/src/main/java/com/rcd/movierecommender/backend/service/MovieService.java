package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieMapper movieMapper;

    public MovieService(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    public Page<MovieDto> searchMovies(String keyword, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "分页参数不合法：page 必须大于等于 0，size 必须为正整数");
        }

        try {
            String normalizedKeyword = normalizeKeyword(keyword);
            int offset = page * size;

            List<MovieEntity> entities = movieMapper.findMovies(normalizedKeyword, size, offset);
            long total = movieMapper.countMovies(normalizedKeyword);
            List<MovieDto> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询电影列表失败", ex);
        }
    }

    public Optional<MovieDto> getMovie(Long id) {
        try {
            return Optional.ofNullable(movieMapper.findById(id)).map(this::toDto);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询电影详情失败", ex);
        }
    }

    public List<MovieDto> getRelatedMovies(Long movieId, int size) {
        if (size <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "size must be positive");
        }
        try {
            MovieEntity currentMovie = movieMapper.findById(movieId);
            if (currentMovie == null) {
                return new ArrayList<>();
            }
            List<String> genreKeywords = extractGenreKeywords(currentMovie.getGenres());
            List<MovieEntity> related = movieMapper.findRelatedMovies(movieId, genreKeywords, size);
            if (related.size() < size) {
                List<MovieEntity> fallback = movieMapper.findRecentMoviesExcluding(movieId, size * 2);
                Set<Long> existingIds = related.stream().map(MovieEntity::getId).collect(Collectors.toCollection(LinkedHashSet::new));
                for (MovieEntity movie : fallback) {
                    if (existingIds.add(movie.getId())) {
                        related.add(movie);
                    }
                    if (related.size() >= size) {
                        break;
                    }
                }
            }
            return related.stream().limit(size).map(this::toDto).collect(Collectors.toList());
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询关联电影失败", ex);
        }
    }

    private List<String> extractGenreKeywords(String genres) {
        if (genres == null || genres.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] rawParts = genres.split("[|,/\\s]+");
        Set<String> normalized = new LinkedHashSet<>();
        for (String rawPart : rawParts) {
            String trimmed = rawPart.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
            if (normalized.size() >= 3) {
                break;
            }
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private MovieDto toDto(MovieEntity entity) {
        return new MovieDto(entity.getId(), entity.getName(), entity.getPublishedYear(), entity.getGenres(), entity.getPosterUrl());
    }
}
