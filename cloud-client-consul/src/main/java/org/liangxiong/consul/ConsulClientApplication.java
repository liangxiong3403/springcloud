package org.liangxiong.consul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liangxiong
 * @Date:2019-03-08
 * @Time:8:50
 * @Description Consul作为客户端
 */
@SpringBootApplication
public class ConsulClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsulClientApplication.class, args);
    }
}
