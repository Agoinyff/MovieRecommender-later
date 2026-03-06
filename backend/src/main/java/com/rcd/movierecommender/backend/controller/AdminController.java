package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.RequireRole;
import com.rcd.movierecommender.backend.dto.AdminStatsDto;
import com.rcd.movierecommender.backend.dto.AuthUserDto;
import com.rcd.movierecommender.backend.dto.UserRole;
import com.rcd.movierecommender.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequireRole(UserRole.ADMIN)
@Tag(name = "Admin", description = "管理员接口")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "后台统计")
    public AdminStatsDto getStats() {
        return adminService.getStats();
    }

    @GetMapping("/users")
    @Operation(summary = "用户列表")
    public List<AuthUserDto> getUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return adminService.listUsers(page, size);
    }
}
