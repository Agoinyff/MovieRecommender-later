package com.rcd.movierecommender.backend.dto;

public class CurrentRatingDto {

    private Long movieId;
    private Double rating;
    private Long timestamp;

    public CurrentRatingDto() {
    }

    public CurrentRatingDto(Long movieId, Double rating, Long timestamp) {
        this.movieId = movieId;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
