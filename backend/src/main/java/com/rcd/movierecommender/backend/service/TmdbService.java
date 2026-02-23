package com.rcd.movierecommender.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcd.movierecommender.backend.config.TmdbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

/**
 * TMDb API 服务，用于获取电影海报
 */
@Service
public class TmdbService {

    private static final Logger log = LoggerFactory.getLogger(TmdbService.class);

    private final TmdbConfig tmdbConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TmdbService(TmdbConfig tmdbConfig) {
        this.tmdbConfig = tmdbConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根据电影名称和年份搜索海报 URL
     * 
     * @param movieName 电影名称
     * @param year      发布年份（可选）
     * @return 海报 URL，如果未找到返回 null
     */
    public String searchMoviePoster(String movieName, String year) {
        if (movieName == null || movieName.trim().isEmpty()) {
            return null;
        }

        try {
            // 构建搜索 URL
            String searchUrl = String.format(
                    "%s/search/movie?api_key=%s&query=%s",
                    tmdbConfig.getBaseUrl(),
                    tmdbConfig.getKey(),
                    java.net.URLEncoder.encode(movieName, "UTF-8"));

            // 如果有年份，添加到搜索参数
            if (year != null && !year.trim().isEmpty()) {
                searchUrl += "&year=" + year;
            }

            log.debug("Searching TMDb for movie: {} ({})", movieName, year);

            // 调用 TMDb API
            String response = restTemplate.getForObject(searchUrl, String.class);

            if (response == null) {
                log.warn("TMDb API returned null response for: {}", movieName);
                return null;
            }

            // 解析 JSON 响应
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            if (results != null && results.isArray() && results.size() > 0) {
                // 获取第一个结果的海报路径
                JsonNode firstResult = results.get(0);
                JsonNode posterPath = firstResult.get("poster_path");

                if (posterPath != null && !posterPath.isNull()) {
                    String posterUrl = tmdbConfig.getImageBaseUrl() + posterPath.asText();
                    log.debug("Found poster for {}: {}", movieName, posterUrl);
                    return posterUrl;
                }
            }

            log.debug("No poster found for movie: {}", movieName);
            return null;

        } catch (RestClientException e) {
            log.error("Error calling TMDb API for movie: {}", movieName, e);
            return null;
        } catch (Exception e) {
            log.error("Error processing TMDb response for movie: {}", movieName, e);
            return null;
        }
    }
}
