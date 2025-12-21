package com.rcd.movierecommender.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Movie Recommender API",
                version = "1.0.0",
                description = "电影推荐系统的 RESTful 接口文档，可通过 /swagger-ui/index.html 进行可视化调试。",
                contact = @Contact(name = "Movie Recommender Team", email = "support@example.com")
        )
)
public class OpenApiConfig {
}
