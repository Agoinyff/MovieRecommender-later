package com.rcd.movierecommender.backend.auth;

import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireAuth requireAuth = resolveRequireAuth(handlerMethod);
        AuthenticatedUser authenticatedUser = resolveUserFromHeader(request);

        if (authenticatedUser != null) {
            AuthContext.setCurrentUser(authenticatedUser);
        }

        if (requireAuth == null) {
            return true;
        }

        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        if (requireAuth.roles().length == 0) {
            return true;
        }

        boolean matched = Arrays.stream(requireAuth.roles())
                .anyMatch(authenticatedUser.getRole()::matches);
        if (!matched) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Insufficient permissions");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private RequireAuth resolveRequireAuth(HandlerMethod handlerMethod) {
        RequireAuth methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),
                RequireAuth.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequireAuth.class);
    }

    private AuthenticatedUser resolveUserFromHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid authorization header");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid authorization header");
        }
        try {
            return jwtService.parseToken(token);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token verification failed", ex);
        }
    }
}
