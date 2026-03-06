package com.rcd.movierecommender.backend.dto;

public class RatingFilterQuery {
    private String query;
    private Double minRating;
    private Double maxRating;
    private int page = 0;
    private int size = 10;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Double getMinRating() {
        return minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public Double getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(Double maxRating) {
        this.maxRating = maxRating;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
