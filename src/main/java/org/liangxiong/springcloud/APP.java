package org.liangxiong.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class APP {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(APP.class);
        springApplication.run(args);
    }

}

