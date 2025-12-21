package com.rcd.movierecommender.backend.dto;

import java.util.Locale;

public enum RecommendationStrategy {
    USER_BASED,
    ITEM_BASED,
    SLOPE_ONE;

    public static RecommendationStrategy fromValue(String value) {
        if (value == null) {
            return USER_BASED;
        }
        try {
            return RecommendationStrategy.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("不支持的推荐策略：" + value + "，可选值：USER_BASED、ITEM_BASED、SLOPE_ONE");
        }
    }
}
