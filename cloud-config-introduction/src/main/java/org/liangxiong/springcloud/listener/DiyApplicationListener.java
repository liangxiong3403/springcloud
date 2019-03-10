package org.liangxiong.springcloud.listener;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.springcloud.event.DiyApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author liangxiong
 * @Date:2018-12-19
 * @Time:21:53
 * @Description 自定义应用监听器, 监听特定事件org.liangxiong.springcloud.event.DiyApplicationEvent
 */
@Slf4j
@Component
public class DiyApplicationListener implements ApplicationListener<DiyApplicationEvent> {

    @Override
    public void onApplicationEvent(DiyApplicationEvent event) {
        log.info("receive event source: {}", event.getSource());
    }
}
