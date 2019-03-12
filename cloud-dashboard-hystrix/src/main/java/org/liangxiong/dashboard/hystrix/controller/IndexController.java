package org.liangxiong.dashboard.hystrix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * @author liangxiong
 * @Date:2019-03-12
 * @Time:10:17
 * @Description 主页
 */
@RestController
public class IndexController {

    private static Random random = new Random();

    @GetMapping("/index")
    public String index() throws TimeoutException {
        int threshold = 5;
        int num = random.nextInt(100);
        if (num > threshold) {
            throw new TimeoutException("api execution timeout");
        }
        return "welcome to hystrix application!";
    }

    @GetMapping("/diyErrorPath")
    public String diyErrorPath() {
        return "server error";
    }
}
