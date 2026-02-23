package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.ModelStatusDto;
import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据模型预热服务
 * 
 * 负责在应用启动后异步构建推荐模型，避免阻塞主流程
 */
@Service
public class ModelWarmupService {

    private static final Logger log = LoggerFactory.getLogger(ModelWarmupService.class);

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CacheManager cacheManager;

    @Value("${model.build.startup-delay:30000}")
    private long startupDelay;

    @Value("${model.build.timeout:300000}")
    private long buildTimeout;

    // 模型构建状态
    private final AtomicReference<ModelStatusDto.BuildStatus> buildStatus = new AtomicReference<>(
            ModelStatusDto.BuildStatus.NOT_STARTED);

    private volatile LocalDateTime lastBuildTime;
    private volatile Long lastBuildDuration;
    private volatile String lastErrorMessage;
    private volatile Integer numUsers;
    private volatile Integer numItems;
    private volatile Long totalRatings;

    /**
     * 应用启动后自动触发预热
     * 延迟启动，避免与其他初始化任务冲突
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupOnStartup() {
        log.info("应用启动完成，将在 {} 秒后开始预热数据模型...", startupDelay / 1000);

        new Thread(() -> {
            try {
                Thread.sleep(startupDelay);
                warmupDataModel();
            } catch (InterruptedException e) {
                log.warn("预热延迟被中断", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 异步预热数据模型
     * 使用 @Async 确保不阻塞调用线程
     */
    @Async
    public void warmupDataModel() {
        if (buildStatus.get() == ModelStatusDto.BuildStatus.BUILDING) {
            log.warn("数据模型正在构建中，跳过本次预热请求");
            return;
        }

        buildStatus.set(ModelStatusDto.BuildStatus.BUILDING);
        log.info("====== 开始异步构建数据模型 ======");

        long startTime = System.currentTimeMillis();

        try {
            // 清除旧缓存
            clearDataModelCache();

            // 重新构建数据模型
            DataModel dataModel = recommendationService.buildDataModel();

            // 手动缓存数据模型
            Cache cache = cacheManager.getCache("dataModel");
            if (cache != null) {
                cache.put("global", dataModel);
                log.info("数据模型已缓存");
            }

            // 获取统计信息
            try {
                numUsers = dataModel.getNumUsers();
                numItems = dataModel.getNumItems();
                // 注意：Mahout DataModel 没有直接获取评分总数的方法
                // 这里暂时置为 null，或者可以通过其他方式统计
                totalRatings = null;
            } catch (Exception e) {
                log.warn("获取数据模型统计信息失败", e);
            }

            long duration = System.currentTimeMillis() - startTime;
            lastBuildDuration = duration;
            lastBuildTime = LocalDateTime.now();
            lastErrorMessage = null;
            buildStatus.set(ModelStatusDto.BuildStatus.SUCCESS);

            log.info("====== 数据模型构建成功 ======");
            log.info("构建耗时: {} 秒", duration / 1000.0);
            log.info("用户数: {}, 电影数: {}", numUsers, numItems);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            lastBuildDuration = duration;
            lastBuildTime = LocalDateTime.now();
            lastErrorMessage = e.getMessage();
            buildStatus.set(ModelStatusDto.BuildStatus.FAILED);

            log.error("====== 数据模型构建失败 ======", e);
            log.error("失败原因: {}", e.getMessage());
        }
    }

    /**
     * 清除数据模型缓存
     */
    private void clearDataModelCache() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            cache.clear();
            log.info("已清除数据模型缓存");
        }
    }

    /**
     * 清除推荐结果缓存
     */
    private void clearRecommendationCache() {
        Cache cache = cacheManager.getCache("recommendations");
        if (cache != null) {
            cache.clear();
            log.info("已清除推荐结果缓存");
        }
    }

    /**
     * 手动触发重建（供管理接口调用）
     */
    public void rebuildModel() {
        log.info("收到手动重建请求");
        clearRecommendationCache();
        warmupDataModel();
    }

    /**
     * 获取模型状态
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
