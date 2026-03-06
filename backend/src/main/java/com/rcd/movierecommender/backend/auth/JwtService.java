package com.rcd.movierecommender.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMillis;
    private SecretKey secretKey;

    public JwtService(@Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-millis:604800000}") long expirationMillis) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(AuthenticatedUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String displayName = claims.get("displayName", String.class);
            String roleValue = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleValue);
            return new AuthenticatedUser(userId, username, displayName, role);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid or expired token", ex);
        }
    }
}
