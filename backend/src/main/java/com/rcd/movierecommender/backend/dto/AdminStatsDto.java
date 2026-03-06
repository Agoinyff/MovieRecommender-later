package com.rcd.movierecommender.backend.dto;

public class AdminStatsDto {
    private long userCount;
    private long ratingCount;
    private long movieCount;
    private ModelStatusDto modelStatus;

    public AdminStatsDto() {
    }

    public AdminStatsDto(long userCount, long ratingCount, long movieCount, ModelStatusDto modelStatus) {
        this.userCount = userCount;
        this.ratingCount = ratingCount;
        this.movieCount = movieCount;
        this.modelStatus = modelStatus;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public long getMovieCount() {
        return movieCount;
    }

    public void setMovieCount(long movieCount) {
        this.movieCount = movieCount;
    }

    public ModelStatusDto getModelStatus() {
        return modelStatus;
    }

    public void setModelStatus(ModelStatusDto modelStatus) {
        this.modelStatus = modelStatus;
    }
}
