package com.rcd.movierecommender.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评分响应 DTO
 * 
 * 用于返回用户的评分记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 电影 ID
     */
    private Long movieId;

    /**
     * 电影名称
     */
    private String movieName;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 评分时间戳
     */
    private Long timestamp;
}
