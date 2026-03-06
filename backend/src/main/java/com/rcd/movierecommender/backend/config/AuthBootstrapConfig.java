package com.rcd.movierecommender.backend.config;

import com.rcd.movierecommender.backend.service.AuthService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthBootstrapConfig {

    @Bean
    public ApplicationRunner bootstrapAdminRunner(AuthService authService) {
        return args -> authService.ensureBootstrapAdmin();
    }
}
