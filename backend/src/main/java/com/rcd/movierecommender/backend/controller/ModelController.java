package com.rcd.movierecommender.backend.controller;

import com.rcd.movierecommender.backend.auth.RequireRole;
import com.rcd.movierecommender.backend.dto.ModelStatusDto;
import com.rcd.movierecommender.backend.dto.UserRole;
import com.rcd.movierecommender.backend.service.ModelWarmupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/model")
@RequireRole(UserRole.ADMIN)
@Tag(name = "Model", description = "数据模型管理接口")
public class ModelController {

    private final ModelWarmupService modelWarmupService;

    public ModelController(ModelWarmupService modelWarmupService) {
        this.modelWarmupService = modelWarmupService;
    }

    @GetMapping("/status")
    @Operation(summary = "查询模型状态")
    public ResponseEntity<ModelStatusDto> getModelStatus() {
        return ResponseEntity.ok(modelWarmupService.getModelStatus());
    }

    @PostMapping("/rebuild")
    @Operation(summary = "重建模型")
    public ResponseEntity<String> rebuildModel() {
        modelWarmupService.rebuildModel();
        return ResponseEntity.ok("模型重建任务已提交");
    }
}
