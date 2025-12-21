package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    /**
     * MovieService 封装了电影查询与 DTO 转换逻辑。
     */
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * 支持分页的电影搜索：当 keyword 为空时返回全部分页；否则执行名称模糊匹配。
     */
    public Page<MovieDto> searchMovies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieEntity> entities;
        if (keyword == null || keyword.trim().isEmpty()) {
            entities = movieRepository.findAll(pageable);
        } else {
            entities = movieRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        return entities.map(this::toDto);
    }

    /**
     * 按主键查询电影并转换为 DTO。
     */
    public Optional<MovieDto> getMovie(Long id) {
        return movieRepository.findById(id).map(this::toDto);
    }

    /**
     * JPA 实体转为传输对象，隔离对外暴露字段。
     */
    private MovieDto toDto(MovieEntity entity) {
        return new MovieDto(entity.getId(), entity.getName(), entity.getPublishedYear(), entity.getGenres());
    }
}
