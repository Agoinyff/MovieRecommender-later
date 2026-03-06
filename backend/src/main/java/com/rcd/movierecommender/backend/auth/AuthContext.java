package com.rcd.movierecommender.backend.auth;

public final class AuthContext {

    private static final ThreadLocal<AuthenticatedUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(AuthenticatedUser user) {
        HOLDER.set(user);
    }

    public static AuthenticatedUser getCurrentUser() {
        return HOLDER.get();
    }

    public static AuthenticatedUser requireCurrentUser() {
        AuthenticatedUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("Current user is not available");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
