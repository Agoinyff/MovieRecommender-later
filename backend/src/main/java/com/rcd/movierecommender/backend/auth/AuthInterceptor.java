package com.rcd.movierecommender.backend.auth;

import com.rcd.movierecommender.backend.dto.JwtUserContext;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Value("${app.auth.cookie-name}")
    private String cookieName;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }

        JwtUserContext userContext = resolveUser(request);
        if (userContext != null) {
            AuthContextHolder.set(userContext);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        if (requireLogin == null) {
            requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if ((requireLogin != null || requireRole != null) && userContext == null) {
            AuthContextHolder.clear();
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        if (requireRole != null && userContext.getRole() != requireRole.value()) {
            AuthContextHolder.clear();
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前用户没有该权限");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private JwtUserContext resolveUser(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return jwtService.parseToken(bearer.substring(7));
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .map(jwtService::parseToken)
                .orElse(null);
    }
}
