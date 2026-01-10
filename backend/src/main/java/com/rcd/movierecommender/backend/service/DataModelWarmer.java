package com.rcd.movierecommender.backend.service;

import org.apache.mahout.cf.taste.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 数据模型预热器
 * 在应用启动后自动在后台异步构建数据模型
 * 避免第一个用户请求时长时间等待
 */
@Component
public class DataModelWarmer {

    private static final Logger log = LoggerFactory.getLogger(DataModelWarmer.class);

    private final FastDataModelBuilder fastBuilder;
    private final CacheManager cacheManager;
    
    // 采样率配置：1.0=全部数据，0.5=50%数据，0.1=10%数据
    // 对于3200万数据，建议从0.3（30%）开始测试
    private static final double SAMPLE_RATE = 0.5;

    public DataModelWarmer(FastDataModelBuilder fastBuilder, CacheManager cacheManager) {
        this.fastBuilder = fastBuilder;
        this.cacheManager = cacheManager;
    }

    /**
     * 应用启动完成后自动触发预热
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmUpDataModel() {
        log.info(new String(new char[80]).replace("\0", "="));
        log.info("应用已启动，开始后台预热数据模型...");
        log.info("采样率: {}（{}%的数据）", SAMPLE_RATE, (int)(SAMPLE_RATE * 100));
        log.info("预热期间，用户请求可能稍慢，请稍候...");
        log.info(new String(new char[80]).replace("\0", "="));

        try {
            long startTime = System.currentTimeMillis();
            
            // 使用快速构建器加载数据
            DataModel dataModel = fastBuilder.buildDataModel(SAMPLE_RATE);
            
            // 手动放入缓存
            Cache cache = cacheManager.getCache("dataModel");
            if (cache != null) {
                cache.put("global", dataModel);
                log.info("数据模型已成功缓存");
            }
            
            long endTime = System.currentTimeMillis();
            log.info(new String(new char[80]).replace("\0", "="));
            log.info("数据模型预热完成！总耗时: {} 秒", (endTime - startTime) / 1000.0);
            log.info("现在可以正常处理推荐请求了");
            log.info(new String(new char[80]).replace("\0", "="));
            
        } catch (Exception e) {
            log.info(new String(new char[80]).replace("\0", "="));
            log.error("数据模型预热失败: {}", e.getMessage(), e);
            log.error("系统仍可正常运行，但首次请求会较慢");
            log.info(new String(new char[80]).replace("\0", "="));
        }
    }

    /**
     * 获取预热状态
     */
    public boolean isWarmedUp() {
        Cache cache = cacheManager.getCache("dataModel");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get("global");
            return wrapper != null && wrapper.get() != null;
        }
        return false;
    }
}

