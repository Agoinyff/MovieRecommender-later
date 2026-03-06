package com.rcd.movierecommender.backend.dto;

public class MovieRecommendationDto {
    private Long movieId;
    private String name;
    private String publishedYear;
    private String genres;
    private String posterUrl;
    private double score;
    private String strategy;

    public MovieRecommendationDto() {
    }

    public MovieRecommendationDto(Long movieId, String name, String publishedYear, String genres, String posterUrl,
            double score, String strategy) {
        this.movieId = movieId;
        this.name = name;
        this.publishedYear = publishedYear;
        this.genres = genres;
        this.posterUrl = posterUrl;
        this.score = score;
        this.strategy = strategy;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
