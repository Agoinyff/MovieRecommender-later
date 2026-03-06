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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieMapper movieMapper;

    public MovieService(MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    public Page<MovieDto> searchMovies(String keyword, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分页参数不合法");
        }
        try {
            String normalizedKeyword = normalizeKeyword(keyword);
            int offset = page * size;
            List<MovieDto> dtos = movieMapper.findMovies(normalizedKeyword, size, offset).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            long total = movieMapper.countMovies(normalizedKeyword);
            return new PageImpl<MovieDto>(dtos, PageRequest.of(page, size), total);
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询电影列表失败", ex);
        }
    }

    public Optional<MovieDto> getMovie(Long id) {
        try {
            MovieEntity entity = movieMapper.findById(id);
            return entity == null ? Optional.<MovieDto>empty() : Optional.of(toDto(entity));
        } catch (DataAccessException ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询电影详情失败", ex);
        }
    }

    public List<MovieDto> getPopularMovies(int size) {
        return movieMapper.findPopularMovies(size).stream().map(this::toDto).collect(Collectors.toList());
    }

    public MovieDto getMovieOrThrow(Long id) {
        return getMovie(id).orElseThrow(new java.util.function.Supplier<BusinessException>() {
            @Override
            public BusinessException get() {
                return new BusinessException(ErrorCode.NOT_FOUND, "电影不存在");
            }
        });
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private MovieDto toDto(MovieEntity entity) {
        return new MovieDto(entity.getId(), entity.getName(), entity.getPublishedYear(), entity.getGenres(),
                entity.getPosterUrl());
    }
}
