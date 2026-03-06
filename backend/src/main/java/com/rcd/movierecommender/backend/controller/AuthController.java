package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.RequireLogin;
import com.rcd.movierecommender.backend.dto.AuthResponse;
import com.rcd.movierecommender.backend.dto.LoginRequest;
import com.rcd.movierecommender.backend.dto.RegisterRequest;
import com.rcd.movierecommender.backend.entity.UserEntity;
import com.rcd.movierecommender.backend.service.AuthService;
import com.rcd.movierecommender.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Auth", description = "认证接口")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${app.auth.cookie-name}")
    private String cookieName;

    @Value("${app.auth.expires-hours:24}")
    private long expiresHours;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request.getUsername(), request.getPassword());
        UserEntity user = userService.getById(response.getUser().getId());
        String token = authService.buildToken(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        authService.buildAuthCookie(cookieName, token, expiresHours * 3600).toString())
                .body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        UserEntity user = userService.getById(response.getUser().getId());
        String token = authService.buildToken(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        authService.buildAuthCookie(cookieName, token, expiresHours * 3600).toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.buildLogoutCookie(cookieName).toString())
                .body(new AuthResponse("已退出登录", null));
    }

    @GetMapping("/me")
    @RequireLogin
    @Operation(summary = "当前登录用户")
    public AuthResponse me() {
        return authService.currentUser();
    }
}
