package com.vd.video.process.common.task;

import com.vd.video.process.controller.VideoController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据更新定时任务
 */
@Slf4j
@Component
public class DataTask {

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateData() {
        Map<String, Integer> dataMap = new HashMap() {{
            put("fileUpdateCount", 0);
            put("playListUpdateCount", 0);
            put("requestCount", 0);
        }};
        VideoController.setDataMap(dataMap);
        log.info("数据更新定时任务执行完成");
    }
}
