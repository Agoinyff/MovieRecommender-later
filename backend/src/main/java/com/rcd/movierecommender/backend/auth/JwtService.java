package com.rcd.movierecommender.backend.auth;

import com.rcd.movierecommender.backend.dto.JwtUserContext;
import com.rcd.movierecommender.backend.dto.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.auth.secret}")
    private String secret;

    @Value("${app.auth.expires-hours:24}")
    private long expiresHours;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(JwtUserContext user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiresHours, ChronoUnit.HOURS);
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtUserContext parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return new JwtUserContext(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class),
                    UserRole.valueOf(claims.get("role", String.class)));
        } catch (JwtException ex) {
            return null;
        }
    }
}
