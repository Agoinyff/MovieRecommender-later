package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.ModelStatusDto;
import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ModelWarmupService {

    private static final Logger log = LoggerFactory.getLogger(ModelWarmupService.class);

    private final RecommendationService recommendationService;
    private final CacheManager cacheManager;

    @Value("${model.build.startup-delay:30000}")
    private long startupDelay;

    @Value("${model.build.timeout:300000}")
    private long buildTimeout;

    private final AtomicReference<ModelStatusDto.BuildStatus> buildStatus =
            new AtomicReference<>(ModelStatusDto.BuildStatus.NOT_STARTED);

    private volatile LocalDateTime lastBuildTime;
    private volatile Long lastBuildDuration;
    private volatile String lastErrorMessage;
    private volatile Integer numUsers;
    private volatile Integer numItems;
    private volatile Long totalRatings;

    /**
     * 负责应用启动后的模型预热、状态维护与手动重建。
     */
    public ModelWarmupService(RecommendationService recommendationService, CacheManager cacheManager) {
        this.recommendationService = recommendationService;
        this.cacheManager = cacheManager;
    }

    /**
     * 应用启动后按延迟时间触发模型预热，避免阻塞主线程启动。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        log.info("Application started. Data model warmup will begin in {} seconds.", startupDelay / 1000);

        Thread warmupThread = new Thread(() -> {
            try {
                Thread.sleep(startupDelay);
                warmupDataModel();
            } catch (InterruptedException ex) {
                log.warn("Warmup delay interrupted", ex);
                Thread.currentThread().interrupt();
            }
        }, "model-warmup-thread");
        warmupThread.setDaemon(true);
        warmupThread.start();
    }

    /**
     * 异步构建并缓存数据模型。
     */
    @Async
    public void warmupDataModel() {
        if (buildStatus.get() == ModelStatusDto.BuildStatus.BUILDING) {
            log.warn("Data model is already building. Skip this warmup request.");
            return;
        }

        buildStatus.set(ModelStatusDto.BuildStatus.BUILDING);
        log.info("====== Start building recommendation data model ======");

        long startTime = System.currentTimeMillis();

        try {
            clearDataModelCache();
            DataModel dataModel = recommendationService.buildDataModel();

            Cache cache = cacheManager.getCache("dataModel");
            if (cache != null) {
                cache.put("global", dataModel);
                log.info("Data model cached successfully.");
            }

            try {
                numUsers = dataModel.getNumUsers();
                numItems = dataModel.getNumItems();
                totalRatings = null;
            } catch (Exception ex) {
                log.warn("Failed to read data model statistics", ex);
            }

            long duration = System.currentTimeMillis() - startTime;
            lastBuildDuration = duration;
            lastBuildTime = LocalDateTime.now();
            lastErrorMessage = null;
            buildStatus.set(ModelStatusDto.BuildStatus.SUCCESS);

            log.info("====== Data model build succeeded ======");
            log.info("Build duration: {}s", duration / 1000.0);
            log.info("Users: {}, Movies: {}", numUsers, numItems);
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            lastBuildDuration = duration;
            lastBuildTime = LocalDateTime.now();
            lastErrorMessage = ex.getMessage();
            buildStatus.set(ModelStatusDto.BuildStatus.FAILED);

            log.error("====== Data model build failed ======", ex);
            log.error("Failure reason: {}", ex.getMessage());
        }
    }

    /**
     * 清理数据模型缓存。
     */
    private void clearDataModelCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            cache.clear();
            log.info("Data model cache cleared.");
        }
    }

    /**
     * 清理推荐结果缓存。
     */
    private void clearRecommendationCache() {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            cache.clear();
            log.info("Recommendation cache cleared.");
        }
    }

    /**
     * 手动触发模型重建，同时清空推荐缓存。
     */
    public void rebuildModel() {
        log.info("Received manual rebuild request.");
        clearRecommendationCache();
        warmupDataModel();
    }

    /**
     * 获取当前模型状态。
     */
    public ModelStatusDto getModelStatus() {
        boolean isReady = buildStatus.get() == ModelStatusDto.BuildStatus.SUCCESS;
        return new ModelStatusDto(
                isReady,
                lastBuildTime,
                lastBuildDuration,
                buildStatus.get(),
                lastErrorMessage,
                numUsers,
                numItems,
                totalRatings);
    }
}