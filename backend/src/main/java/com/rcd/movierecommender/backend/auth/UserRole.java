package com.rcd.movierecommender.backend.auth;

public enum UserRole {
    USER,
    ADMIN;

    public boolean matches(UserRole requiredRole) {
        return this == requiredRole;
    }
}
