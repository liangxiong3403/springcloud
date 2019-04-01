package org.liangxiong.ribbon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.event.EnvironmentChangeRemoteApplicationEvent;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.bus.event.SentApplicationEvent;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * @author liangxiong
 * @Date:2019-04-01
 * @Time:9:26
 * @Description Spring Cloud Bus 配置类
 */
@RemoteApplicationEventScan
@Slf4j
@Configuration
public class BusConfiguration {

    /**
     * 监听特定事件{@link RefreshRemoteApplicationEvent}
     *
     * @param event
     */
    @EventListener
    public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
        log.info("RefreshRemoteApplicationEvent source: {}, originService: {}, destinationService: {}", event.getSource(), event.getOriginService(), event.getDestinationService());
    }

    /**
     * 监听特定事件{@link EnvironmentChangeRemoteApplicationEvent}
     *
     * @param event
     */
    @EventListener
    public void onEnvironmentChangeRemoteApplicationEvent(EnvironmentChangeRemoteApplicationEvent event) {
        log.info("EnvironmentChangeRemoteApplicationEvent source: {}, originService: {}, destinationService: {}", event.getSource(), event.getOriginService(), event.getDestinationService());
    }

    /**
     * 监听特定事件{@link SentApplicationEvent }
     *
     * @param event
     */
    @EventListener
    public void onSentApplicationEvent(SentApplicationEvent event) {
        log.info("SentApplicationEvent  source: {}, originService: {}, destinationService: {}", event.getSource(), event.getOriginService(), event.getDestinationService());
    }

    /**
     * 监听特定事件{@link AckRemoteApplicationEvent }
     *
     * @param event
     */
    @EventListener
    public void onAckRemoteApplicationEvent(AckRemoteApplicationEvent event) {
        log.info("AckRemoteApplicationEvent  source: {}, originService: {}, destinationService: {}", event.getSource(), event.getOriginService(), event.getDestinationService());
    }

}
