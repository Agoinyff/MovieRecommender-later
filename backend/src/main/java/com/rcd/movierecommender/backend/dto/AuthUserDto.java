package com.rcd.movierecommender.backend.dto;

import java.time.LocalDateTime;

public class AuthUserDto {
    private Long id;
    private String username;
    private UserRole role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public AuthUserDto() {
    }

    public AuthUserDto(Long id, String username, UserRole role, String status, LocalDateTime createdAt,
            LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
