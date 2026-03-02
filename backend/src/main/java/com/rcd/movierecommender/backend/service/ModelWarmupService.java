package com.rcd.movierecommender.backend.service;

import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 数据模型预热服务
 *
 * 负责异步（重）建推荐所需的 DataModel 并写入 Spring Cache，
 * 供 RecommendationService 从缓存中读取，实现非阻塞推荐。
 */
@Service
public class ModelWarmupService {

    private static final Logger log = LoggerFactory.getLogger(ModelWarmupService.class);

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 异步重建数据模型并写入 "dataModel" 缓存。
     * 使用 @Async，调用方不阻塞等待结果。
     * 若上一次构建尚未完成，本次调用仍会触发新一轮构建（以最新评分为准）。
     */
    @Async
    public void warmupDataModel() {
        log.info("开始异步重建数据模型...");
        try {
            DataModel dataModel = recommendationService.buildDataModel();

            Cache cache = cacheManager.getCache("dataModel");
            if (cache != null) {
                cache.put("global", dataModel);
                log.info("数据模型已更新到缓存");
            } else {
                log.warn("未找到名为 'dataModel' 的缓存，数据模型未能写入缓存");
            }
        } catch (Exception e) {
            log.error("异步重建数据模型失败", e);
        }
    }
}
