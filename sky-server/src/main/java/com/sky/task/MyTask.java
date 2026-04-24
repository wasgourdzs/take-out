package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyTask {

//    @Scheduled(cron = "0/5 * * * * *")
    public void execution () {
        log.info("任务执行时间：{}", LocalDateTime.now());
    }
}
