package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.AuthUserDto;
import com.rcd.movierecommender.backend.dto.UserRole;
import com.rcd.movierecommender.backend.entity.UserEntity;
import com.rcd.movierecommender.backend.exception.BusinessException;
import com.rcd.movierecommender.backend.exception.ErrorCode;
import com.rcd.movierecommender.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123456}")
    private String adminPassword;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void ensureAdminUser() {
        UserEntity existing = userMapper.findByUsername(adminUsername);
        if (existing != null) {
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setId(userMapper.nextUserId());
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN.name());
        admin.setStatus("ACTIVE");
        admin.setCreatedAt(LocalDateTime.now());
        userMapper.insert(admin);
    }

    public UserEntity register(String username, String rawPassword) {
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        UserEntity user = new UserEntity();
        user.setId(userMapper.nextUserId());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.USER.name());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    public UserEntity authenticate(String username, String rawPassword) {
        UserEntity user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());
        return userMapper.findById(user.getId());
    }

    public UserEntity getById(Long id) {
        UserEntity user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或登录已失效");
        }
        return user;
    }

    public long countUsers() {
        return userMapper.countUsers();
    }

    public List<AuthUserDto> listUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        return userMapper.findUsers(safeSize, safePage * safeSize).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AuthUserDto toDto(UserEntity user) {
        return new AuthUserDto(
                user.getId(),
                user.getUsername(),
                UserRole.valueOf(user.getRole()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLoginAt());
    }
}
