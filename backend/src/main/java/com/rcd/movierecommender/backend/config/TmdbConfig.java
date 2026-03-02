package com.rcd.movierecommender.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TMDb API 配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "tmdb.api")
public class TmdbConfig {

    private String key;
    private String readToken;
    private String baseUrl;
    private String imageBaseUrl;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReadToken() {
        return readToken;
    }

    public void setReadToken(String readToken) {
        this.readToken = readToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    public void setImageBaseUrl(String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }
}
