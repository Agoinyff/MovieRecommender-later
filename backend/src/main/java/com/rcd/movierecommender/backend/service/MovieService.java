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

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

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

    public Optional<MovieDto> getMovie(Long id) {
        return movieRepository.findById(id).map(this::toDto);
    }

    private MovieDto toDto(MovieEntity entity) {
        return new MovieDto(entity.getId(), entity.getName(), entity.getPublishedYear(), entity.getGenres());
    }
}
