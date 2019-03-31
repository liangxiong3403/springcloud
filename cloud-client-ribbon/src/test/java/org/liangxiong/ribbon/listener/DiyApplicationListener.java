package org.liangxiong.ribbon.listener;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.ribbon.event.DiyApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author liangxiong
 * @Date:2019-03-31
 * @Time:20:09
 * @Description
 */
@Slf4j
public class DiyApplicationListener implements ApplicationListener<DiyApplicationEvent> {

    @Override
    public void onApplicationEvent(DiyApplicationEvent event) {
        log.info("receive event: {}", event.getSource());
    }
}
