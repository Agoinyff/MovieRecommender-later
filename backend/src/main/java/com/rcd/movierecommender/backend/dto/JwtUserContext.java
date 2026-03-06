package com.rcd.movierecommender.backend.dto;

public class JwtUserContext {
    private final Long userId;
    private final String username;
    private final UserRole role;

    public JwtUserContext(Long userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN == role;
    }
}
