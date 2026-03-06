package com.rcd.movierecommender.backend.dto;

public class AuthResponse {
    private String message;
    private AuthUserDto user;

    public AuthResponse() {
    }

    public AuthResponse(String message, AuthUserDto user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthUserDto getUser() {
        return user;
    }

    public void setUser(AuthUserDto user) {
        this.user = user;
    }
}
