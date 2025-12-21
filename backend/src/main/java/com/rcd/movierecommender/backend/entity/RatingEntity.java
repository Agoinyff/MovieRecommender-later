package com.rcd.movierecommender.backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "movie_preferences")
@IdClass(RatingId.class)
public class RatingEntity {

    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "movieID")
    private Long movieId;

    @Column(nullable = false)
    private Integer preference;

    @Column(nullable = false)
    private Long timestamp;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Integer getPreference() {
        return preference;
    }

    public void setPreference(Integer preference) {
        this.preference = preference;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
