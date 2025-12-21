package com.rcd.movierecommender.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class RatingId implements Serializable {

    private Long userId;
    private Long movieId;

    public RatingId() {
    }

    public RatingId(Long userId, Long movieId) {
        this.userId = userId;
        this.movieId = movieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RatingId ratingId = (RatingId) o;
        return Objects.equals(userId, ratingId.userId) && Objects.equals(movieId, ratingId.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, movieId);
    }
}
