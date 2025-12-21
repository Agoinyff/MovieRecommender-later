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
        return RecommendationStrategy.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
