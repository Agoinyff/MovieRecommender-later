package com.rcd.movierecommender.backend.dto;

public class MovieDto {
    private Long id;
    private String name;
    private String publishedYear;
    private String genres;

    public MovieDto() {
    }

    public MovieDto(Long id, String name, String publishedYear, String genres) {
        this.id = id;
        this.name = name;
        this.publishedYear = publishedYear;
        this.genres = genres;
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
}
