package org.liangxiong.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liangxiong
 * @Desciption 启动类
 */
@SpringBootApplication
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ConfigClientApplication.class);
        springApplication.run(args);
    }

}

