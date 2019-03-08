package org.liangxiong.eureka.highavailable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author liangxiong
 * @Date:2019-03-08
 * @Time:14:33
 * @Description eureka服务端高可用
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerHighAvailableApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerHighAvailableApplication.class, args);
    }
}
