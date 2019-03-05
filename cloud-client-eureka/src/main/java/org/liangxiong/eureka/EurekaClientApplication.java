package org.liangxiong.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:10:06
 * @Description eureka客户端;可以使用@EnableEurekaClient或@EnableDiscoveryClient
 */
@EnableEurekaClient
@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}
