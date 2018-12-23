package org.liangxiong.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liangxiong
 * @Desciption 启动类
 */
@SpringBootApplication
public class SpringCloudApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SpringCloudApplication.class);
        springApplication.run(args);
    }

}

