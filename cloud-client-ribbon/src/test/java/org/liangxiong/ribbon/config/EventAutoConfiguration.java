package org.liangxiong.ribbon.config;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.ribbon.event.DiyApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * @author liangxiong
 * @Date:2019-03-31
 * @Time:20:17
 * @Description
 */
@Slf4j
@Configuration
public class EventAutoConfiguration {

    /**
     * 监听自定义事件{@link DiyApplicationEvent}
     *
     * @param event
     */
    @EventListener
    public void onDiyEvent(DiyApplicationEvent event) {
        log.info("receive event: {}", event.getSource());
    }
}
