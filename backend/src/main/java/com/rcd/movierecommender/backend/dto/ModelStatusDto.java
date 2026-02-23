package com.rcd.movierecommender.backend.dto;

import java.time.LocalDateTime;

/**
 * 数据模型构建状态 DTO
 * 用于向前端展示推荐模型的当前状态
 */
public class ModelStatusDto {

    /**
     * 构建状态枚举
     */
    public enum BuildStatus {
        SUCCESS, // 构建成功
        BUILDING, // 正在构建中
        FAILED, // 构建失败
        NOT_STARTED // 尚未开始
    }

    private boolean isReady; // 模型是否可用
    private LocalDateTime lastBuildTime; // 上次构建时间
    private Long lastBuildDuration; // 构建耗时（毫秒）
    private BuildStatus buildStatus; // 构建状态
    private String errorMessage; // 错误信息（仅在失败时）
    private Integer numUsers; // 用户数量
    private Integer numItems; // 电影数量
    private Long totalRatings; // 评分总数

    public ModelStatusDto() {
    }

    public ModelStatusDto(boolean isReady, LocalDateTime lastBuildTime, Long lastBuildDuration,
            BuildStatus buildStatus, String errorMessage, Integer numUsers,
            Integer numItems, Long totalRatings) {
        this.isReady = isReady;
        this.lastBuildTime = lastBuildTime;
        this.lastBuildDuration = lastBuildDuration;
        this.buildStatus = buildStatus;
        this.errorMessage = errorMessage;
        this.numUsers = numUsers;
        this.numItems = numItems;
        this.totalRatings = totalRatings;
    }

    // Getters and Setters
    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public LocalDateTime getLastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(LocalDateTime lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public Long getLastBuildDuration() {
        return lastBuildDuration;
    }

    public void setLastBuildDuration(Long lastBuildDuration) {
        this.lastBuildDuration = lastBuildDuration;
    }

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(Integer numUsers) {
        this.numUsers = numUsers;
    }

    public Integer getNumItems() {
        return numItems;
    }

    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Long totalRatings) {
        this.totalRatings = totalRatings;
    }
}
