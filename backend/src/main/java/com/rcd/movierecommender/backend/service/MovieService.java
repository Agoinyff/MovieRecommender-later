package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        if (page < 0 || size <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "分页参数不合法：page 必须大于等于 0，size 必须为正整数");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MovieEntity> entities;
            if (keyword == null || keyword.trim().isEmpty()) {
                entities = movieRepository.findAll(pageable);
            } else {
                entities = movieRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
            }
            return entities.map(this::toDto);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "查询电影列表失败，数据库访问发生异常", e);
        }
    }

    /**
     * 按主键查询电影并转换为 DTO。
     */
    public Optional<MovieDto> getMovie(Long id) {
        try {
            return movieRepository.findById(id).map(this::toDto);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR,
                    "查询电影详情失败，数据库访问发生异常", e);
        }
    }

    /**
     * JPA 实体转为传输对象，隔离对外暴露字段。
     */
    private MovieDto toDto(MovieEntity entity) {
        return new MovieDto(entity.getId(), entity.getName(), entity.getPublishedYear(), entity.getGenres());
    }
}
