package com.rcd.movierecommender.backend.repository;

import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface RatingRepository extends JpaRepository<RatingEntity, RatingId> {

    List<RatingEntity> findByUserId(Long userId);

    List<RatingEntity> findByMovieId(Long movieId);

    @Query("select distinct r.userId from RatingEntity r")
    Set<Long> findDistinctUserIds();
}
