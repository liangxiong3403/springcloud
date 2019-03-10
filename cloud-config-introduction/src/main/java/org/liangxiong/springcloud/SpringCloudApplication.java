package org.liangxiong.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * @author liangxiong
 * @Desciption 启动类
 */
@SpringBootApplication
public class SpringCloudApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SpringCloudApplication.class);
        springApplication.setDefaultProperties(Collections.singletonMap("location", "chengdu"));
        springApplication.run(args);
    }

}

