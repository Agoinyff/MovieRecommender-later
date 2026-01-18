package com.rcd.movierecommender.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 评分请求 DTO
 * 
 * 用于用户提交电影评分
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 电影 ID
     */
    @NotNull(message = "电影 ID 不能为空")
    private Long movieId;

    /**
     * 评分（1-5）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小值为 1")
    @Max(value = 5, message = "评分最大值为 5")
    private Double rating;
}
