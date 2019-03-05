package org.liangxiong.eureka.controller;

import org.liangxiong.eureka.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangxiong
 * @Date:2019-03-04
 * @Time:11:20
 * @Description 主页控制器
 */
@RestController
@EnableConfigurationProperties(User.class)
@RequestMapping("/index")
public class IndexController {

    private final User user;

    @Autowired
    public IndexController(User user) {
        this.user = user;
    }

    @GetMapping("/config/user")
    public User getUserFromConfig() {
        return user;
    }

}
