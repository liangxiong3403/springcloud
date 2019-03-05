package org.liangxiong.eureka.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:15:40
 * @Description spring config server作为eureka客户端
 */
@EnableDiscoveryClient
@EnableConfigServer
@SpringBootApplication
public class ConfigServerAsEurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerAsEurekaClientApplication.class, args);
    }
}
