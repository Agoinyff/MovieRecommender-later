package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.auth.AuthenticatedUser;
import com.rcd.movierecommender.backend.auth.JwtService;
import com.rcd.movierecommender.backend.auth.UserRole;
import com.rcd.movierecommender.backend.dto.AuthResponse;
import com.rcd.movierecommender.backend.dto.LoginRequest;
import com.rcd.movierecommender.backend.dto.RegisterRequest;
import com.rcd.movierecommender.backend.dto.UserProfileDto;
import com.rcd.movierecommender.backend.entity.UserEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String bootstrapAdminUsername;
    private final String bootstrapAdminPassword;
    private final String bootstrapAdminDisplayName;

    public AuthService(UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${auth.bootstrap-admin.username:admin}") String bootstrapAdminUsername,
            @Value("${auth.bootstrap-admin.password:admin123}") String bootstrapAdminPassword,
            @Value("${auth.bootstrap-admin.display-name:System Admin}") String bootstrapAdminDisplayName) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.bootstrapAdminUsername = bootstrapAdminUsername;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
        this.bootstrapAdminDisplayName = bootstrapAdminDisplayName;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Username already exists");
        }

        long now = System.currentTimeMillis() / 1000;
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName().trim());
        user.setRole(UserRole.USER.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userMapper.findByUsername(request.getUsername().trim());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Username or password is incorrect");
        }
        return buildAuthResponse(user);
    }

    public UserProfileDto getUserProfile(Long userId) {
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Current user does not exist");
        }
        return new UserProfileDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }

    @Transactional
    public void ensureBootstrapAdmin() {
        UserEntity existing = userMapper.findByUsername(bootstrapAdminUsername);
        if (existing != null) {
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        UserEntity admin = new UserEntity();
        admin.setUsername(bootstrapAdminUsername);
        admin.setPasswordHash(passwordEncoder.encode(bootstrapAdminPassword));
        admin.setDisplayName(bootstrapAdminDisplayName);
        admin.setRole(UserRole.ADMIN.name());
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        userMapper.insert(admin);
        log.info("Bootstrap admin account created: {}", bootstrapAdminUsername);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        AuthenticatedUser principal = new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                UserRole.valueOf(user.getRole()));
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, new UserProfileDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole()));
    }
}
