package org.liangxiong.server.provider.controller;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:06
 * @Description 用户服务provider
 */
@Slf4j
@RequestMapping("/users")
@RestController
public class UserProviderNativeController {

    @Value("${server.port}")
    private Integer port;

    @Autowired
    @Qualifier("inMemoryUserServiceImpl")
    private IUserService userService;

    private static Random random = new Random();

    /**
     * 添加用户
     *
     * @param user 用户实体
     * @return
     */
    @PostMapping
    public Object addUser(@RequestBody User user) {
        JSONObject result = new JSONObject(8);
        result.put("username", user.getUsername());
        result.put("age", user.getAge());
        result.put("userId", user.getUserId());
        // 区分服务端
        result.put("serverPort", port);
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(50));
        } catch (InterruptedException e) {
            log.error("thread interruption: {}", e.getMessage());
        }
        return userService.addUser(user) ? result : new HashMap<Integer, Object>(8);
    }

    /**
     * 获取所有用户数据
     * <p>
     * 使用HystrixProperty设置超时时间
     *
     * @return
     */
    @HystrixCommand(fallbackMethod = "listAllUserFallback", commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")})
    @GetMapping
    public List<User> listAllUser() {
        // 模拟超时
        try {
            int milliseconds = random.nextInt(500);
            log.info("server execution milliseconds: {}", milliseconds);
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("thread interruption: {}", e.getMessage());
        }
        return userService.listAllUsers();
    }

    /**
     * 超时发生以后地回调方法
     *
     * @return
     */
    private List<User> listAllUserFallback() {
        log.error("server execution timeout!");
        return Collections.emptyList();
    }

}
