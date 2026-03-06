package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.AdminStatsDto;
import com.rcd.movierecommender.backend.dto.AuthUserDto;
import com.rcd.movierecommender.backend.mapper.MovieMapper;
import com.rcd.movierecommender.backend.mapper.RatingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserService userService;
    private final RatingMapper ratingMapper;
    private final MovieMapper movieMapper;
    private final ModelWarmupService modelWarmupService;

    public AdminService(UserService userService, RatingMapper ratingMapper, MovieMapper movieMapper,
            ModelWarmupService modelWarmupService) {
        this.userService = userService;
        this.ratingMapper = ratingMapper;
        this.movieMapper = movieMapper;
        this.modelWarmupService = modelWarmupService;
    }

    public AdminStatsDto getStats() {
        return new AdminStatsDto(
                userService.countUsers(),
                ratingMapper.countRatings(),
                movieMapper.countMovies(null),
                modelWarmupService.getModelStatus());
    }

    public List<AuthUserDto> listUsers(int page, int size) {
        return userService.listUsers(page, size);
    }
}
