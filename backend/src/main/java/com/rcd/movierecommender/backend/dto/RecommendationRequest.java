package com.rcd.movierecommender.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 推荐请求 DTO。
 * 普通用户不需要传 userId；管理员可以传入 userId 查询指定用户的推荐结果。
 */
public class RecommendationRequest {

    @Schema(description = "目标用户 ID。普通用户留空时默认使用当前登录用户；管理员可指定用户 ID。", example = "1000002")
    private Long userId;

    @Min(1)
    @Max(50)
    @Schema(description = "返回条数，范围 1-50。", example = "10", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "推荐策略，可选 USER_BASED、ITEM_BASED、SLOPE_ONE。", example = "USER_BASED")
    private String strategy;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}