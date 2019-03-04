package org.liangxiong.springcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author liangxiong
 * @Desciption 启动类
 */
@Slf4j
@SpringBootApplication
public class ConfigClientApplication {

    private final ContextRefresher contextRefresher;

    @Autowired
    public ConfigClientApplication(ContextRefresher contextRefresher) {
        this.contextRefresher = contextRefresher;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }

    /**
     * 定时刷新配置服务器文件
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    public void refreshServerConfig() {
        Set<String> keys = contextRefresher.refresh();
        if (!CollectionUtils.isEmpty(keys)) {
            log.info("server config changes: {}", keys);
        }
    }

}

