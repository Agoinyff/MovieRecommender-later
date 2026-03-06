package com.rcd.movierecommender.backend.auth;

import com.rcd.movierecommender.backend.dto.JwtUserContext;

public final class AuthContextHolder {
    private static final ThreadLocal<JwtUserContext> CONTEXT = new ThreadLocal<JwtUserContext>();

    private AuthContextHolder() {
    }

    public static void set(JwtUserContext userContext) {
        CONTEXT.set(userContext);
    }

    public static JwtUserContext get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        JwtUserContext context = get();
        return context == null ? null : context.getUserId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
