package com.rcd.movierecommender.backend.dto;

public class RecommendationDto {
    private Long movieId;
    private String name;
    private String publishedYear;
    private String genres;
    private double score;

    public RecommendationDto() {
    }

    public RecommendationDto(Long movieId, String name, String publishedYear, String genres, double score) {
        this.movieId = movieId;
        this.name = name;
        this.publishedYear = publishedYear;
        this.genres = genres;
        this.score = score;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(String publishedYear) {
        this.publishedYear = publishedYear;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
