package com.rcd.movierecommender.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 评分请求 DTO。
 * 当前登录用户对电影提交评分时使用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    @Schema(description = "电影 ID。", example = "1")
    @NotNull(message = "电影 ID 不能为空")
    private Long movieId;

    @Schema(description = "评分值，范围 1-5。", example = "4.5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小值为 1")
    @Max(value = 5, message = "评分最大值为 5")
    private Double rating;
}