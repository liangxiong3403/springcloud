package org.liangxiong.server.provider;

import org.liangxiong.server.provider.stream.ActiveMessageStream;
import org.liangxiong.server.provider.stream.UserMessageStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.stream.annotation.EnableBinding;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:04
 * @Description 用户服务提供者, Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
 */
@EnableBinding({UserMessageStream.class, ActiveMessageStream.class})
@EnableHystrix
@EnableDiscoveryClient
@SpringBootApplication
public class UserProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApplication.class, args);
    }
}
