package org.liangxiong.consul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author liangxiong
 * @Date:2019-03-08
 * @Time:8:50
 * @Description Consul作为客户端, 从Edgware版本开始EnableDiscoveryClient注解不是必须地
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ConsulClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsulClientApplication.class, args);
    }
}
