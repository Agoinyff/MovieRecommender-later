package com.rcd.movierecommender.backend.repository;

import com.rcd.movierecommender.backend.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {
    Page<MovieEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
