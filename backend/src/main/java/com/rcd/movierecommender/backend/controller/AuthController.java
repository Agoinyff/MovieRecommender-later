package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.AuthContext;
import com.rcd.movierecommender.backend.auth.RequireAuth;
import com.rcd.movierecommender.backend.dto.AuthResponse;
import com.rcd.movierecommender.backend.dto.LoginRequest;
import com.rcd.movierecommender.backend.dto.RegisterRequest;
import com.rcd.movierecommender.backend.dto.UserProfileDto;
import com.rcd.movierecommender.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "登录、注册与当前登录用户接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 构造函数注入认证服务。
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册新用户。
     *
     * @param request 注册请求。
     * @return 注册成功后的登录态与用户信息。
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建普通用户账号，并在注册成功后直接返回 JWT 与用户信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "参数校验失败或用户名已存在", content = @Content)
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求。
     * @return 登录成功后的 JWT 与用户信息。
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，并返回 JWT 与当前用户信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误", content = @Content)
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户资料。
     */
    @RequireAuth
    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "根据 JWT 返回当前登录用户的基础资料与角色信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public UserProfileDto me() {
        return authService.getUserProfile(AuthContext.requireCurrentUser().getId());
    }
}