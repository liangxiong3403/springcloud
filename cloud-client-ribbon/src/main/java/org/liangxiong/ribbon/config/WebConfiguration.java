package org.liangxiong.ribbon.config;

import org.liangxiong.cloud.api.fallback.UserServiceFallback;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:13:55
 * @Description 配置类
 */
@Configuration
public class WebConfiguration {

    /**
     * 实现RestTemplate实例的负载均衡
     *
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 解决feign中开启hystrix的报错
     *
     * @return
     */
    @Bean
    public UserServiceFallback userServiceFallback() {
        return new UserServiceFallback();
    }
}
