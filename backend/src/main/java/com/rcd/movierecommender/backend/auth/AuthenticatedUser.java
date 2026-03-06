package com.rcd.movierecommender.backend.auth;

public class AuthenticatedUser {

    private final Long id;
    private final String username;
    private final String displayName;
    private final UserRole role;

    public AuthenticatedUser(Long id, String username, String displayName, UserRole role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
