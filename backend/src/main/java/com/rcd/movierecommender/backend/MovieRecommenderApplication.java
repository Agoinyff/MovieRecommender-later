package com.rcd.movierecommender.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.rcd.movierecommender.backend.mapper")
public class MovieRecommenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRecommenderApplication.class, args);
    }
}
