package org.liangxiong.stream.binder.activemq.config;

import org.liangxiong.stream.binder.activemq.ActiveMessageChannelBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liangxiong
 * @Date:2019-03-29
 * @Time:10:00
 * @Description 自动装配ActiveMessageChannelBinder的bean
 */
@ConditionalOnMissingBean(ActiveMessageChannelBinder.class)
@Configuration
public class ActiveBinderConfiguration {

    @Bean
    public ActiveMessageChannelBinder activeMessageChannelBinder() {
        return new ActiveMessageChannelBinder();
    }
}

