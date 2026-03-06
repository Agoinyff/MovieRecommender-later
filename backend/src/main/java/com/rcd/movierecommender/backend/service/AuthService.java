package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.auth.AuthContextHolder;
import com.rcd.movierecommender.backend.auth.JwtService;
import com.rcd.movierecommender.backend.dto.AuthResponse;
import com.rcd.movierecommender.backend.dto.JwtUserContext;
import com.rcd.movierecommender.backend.entity.UserEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(String username, String password) {
        UserEntity user = userService.register(username, password);
        return new AuthResponse("注册成功", userService.toDto(user));
    }

    public AuthResponse login(String username, String password) {
        UserEntity user = userService.authenticate(username, password);
        return new AuthResponse("登录成功", userService.toDto(user));
    }

    public String buildToken(UserEntity user) {
        return jwtService.generateToken(new JwtUserContext(user.getId(), user.getUsername(),
                com.rcd.movierecommender.backend.dto.UserRole.valueOf(user.getRole())));
    }

    public ResponseCookie buildAuthCookie(String cookieName, String token, long maxAgeSeconds) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie buildLogoutCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    public AuthResponse currentUser() {
        Long userId = AuthContextHolder.getUserId();
        UserEntity user = userService.getById(userId);
        return new AuthResponse("获取成功", userService.toDto(user));
    }
}
