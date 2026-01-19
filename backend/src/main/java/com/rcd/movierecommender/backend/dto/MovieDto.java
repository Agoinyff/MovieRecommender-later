package com.rcd.movierecommender.backend.dto;

public class MovieDto {
    private Long id;
    private String name;
    private String publishedYear;
    private String genres;
    private String posterUrl;

    public MovieDto() {
    }

    public MovieDto(Long id, String name, String publishedYear, String genres, String posterUrl) {
        this.id = id;
        this.name = name;
        this.publishedYear = publishedYear;
        this.genres = genres;
        this.posterUrl = posterUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
