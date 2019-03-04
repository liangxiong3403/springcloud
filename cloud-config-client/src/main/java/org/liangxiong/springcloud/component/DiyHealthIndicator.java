package org.liangxiong.springcloud.component;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

/**
 * @author liangxiong
 * @Date:2019-03-04
 * @Time:15:22
 * @Description 自定义健康标示器
 */
@Component
public class DiyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // 设置状态码
        Health.Builder builder = Health.status(Status.UP);
        builder.withDetail("name", "DiyHealthIndicator");
        builder.withDetail("timestamp", System.currentTimeMillis());
        return builder.build();
    }
}
