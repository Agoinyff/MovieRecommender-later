package com.rcd.movierecommender.backend.task;

import com.rcd.movierecommender.backend.service.ModelWarmupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据模型定时构建任务
 * 
 * 定期刷新推荐模型，确保数据实时性
 */
@Component
public class ModelBuildTask {

    private static final Logger log = LoggerFactory.getLogger(ModelBuildTask.class);

    @Autowired
    private ModelWarmupService modelWarmupService;

    /**
     * 每小时执行一次模型重建
     * cron 表达式：秒 分 时 日 月 周
     * 0 0 * * * ? = 每小时的第0分0秒执行
     */
    @Scheduled(cron = "${model.build.cron:0 0 * * * ?}")
    public void scheduledModelRebuild() {
        log.info("====== 定时任务触发：开始重建数据模型 ======");
        try {
            modelWarmupService.warmupDataModel();
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }
}
