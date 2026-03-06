package com.rcd.movierecommender.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求 DTO。
 */
public class RegisterRequest {

    @Schema(description = "注册用户名，仅允许字母、数字和下划线。", example = "movie_user")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在 3 到 32 个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "登录密码。", example = "movie123")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在 6 到 64 个字符之间")
    private String password;

    @Schema(description = "展示名称。", example = "影迷小王")
    @NotBlank(message = "展示名称不能为空")
    @Size(min = 2, max = 32, message = "展示名称长度必须在 2 到 32 个字符之间")
    private String displayName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}