package org.liangxiong.eureka.highavailable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author liangxiong
 * @Date:2019-03-08
 * @Time:9:15
 * @Description eureka高可用客户端
 */
@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientHighAvailableApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientHighAvailableApplication.class, args);
    }
}
