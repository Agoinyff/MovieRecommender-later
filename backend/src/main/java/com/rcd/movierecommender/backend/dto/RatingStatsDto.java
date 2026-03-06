package com.rcd.movierecommender.backend.dto;

public class RatingStatsDto {
    private long totalRatings;
    private double averageRating;
    private long highRatingsCount;
    private Long recentTimestamp;

    public RatingStatsDto() {
    }

    public RatingStatsDto(long totalRatings, double averageRating, long highRatingsCount, Long recentTimestamp) {
        this.totalRatings = totalRatings;
        this.averageRating = averageRating;
        this.highRatingsCount = highRatingsCount;
        this.recentTimestamp = recentTimestamp;
    }

    public long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getHighRatingsCount() {
        return highRatingsCount;
    }

    public void setHighRatingsCount(long highRatingsCount) {
        this.highRatingsCount = highRatingsCount;
    }

    public Long getRecentTimestamp() {
        return recentTimestamp;
    }

    public void setRecentTimestamp(Long recentTimestamp) {
        this.recentTimestamp = recentTimestamp;
    }
}
