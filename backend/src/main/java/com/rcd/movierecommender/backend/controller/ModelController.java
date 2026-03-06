package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.RequireAuth;
import com.rcd.movierecommender.backend.auth.UserRole;
import com.rcd.movierecommender.backend.dto.ModelStatusDto;
import com.rcd.movierecommender.backend.service.ModelWarmupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据模型管理接口。
 * 提供模型状态查询和手动重建功能。
 */
@RestController
@RequestMapping("/api/model")
@RequireAuth(roles = { UserRole.ADMIN })
@Tag(name = "Model", description = "数据模型管理接口")
public class ModelController {

    private final ModelWarmupService modelWarmupService;

    /**
     * 构造函数注入模型预热服务。
     */
    public ModelController(ModelWarmupService modelWarmupService) {
        this.modelWarmupService = modelWarmupService;
    }

    /**
     * 查询数据模型状态。
     *
     * @return 模型状态信息。
     */
    @GetMapping("/status")
    @Operation(summary = "查询模型状态", description = "获取推荐数据模型的当前构建状态和统计信息，仅管理员可访问。")
    public ResponseEntity<ModelStatusDto> getModelStatus() {
        return ResponseEntity.ok(modelWarmupService.getModelStatus());
    }

    /**
     * 手动触发模型重建。
     *
     * @return 成功响应。
     */
    @PostMapping("/rebuild")
    @Operation(summary = "手动重建模型", description = "立即触发数据模型的异步重建，仅管理员可访问。")
    public ResponseEntity<String> rebuildModel() {
        modelWarmupService.rebuildModel();
        return ResponseEntity.ok("模型重建任务已提交，正在后台执行");
    }
}