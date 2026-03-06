package com.rcd.movierecommender.backend.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class RatingRequest {

    @NotNull(message = "电影 ID 不能为空")
    private Long movieId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小值为 1")
    @Max(value = 5, message = "评分最大值为 5")
    private Double rating;

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
}
