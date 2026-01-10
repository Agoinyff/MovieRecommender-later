package com.rcd.movierecommender.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // 启用异步支持（用于后台预热）
public class MovieRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRecommenderApplication.class, args);
    }
}
