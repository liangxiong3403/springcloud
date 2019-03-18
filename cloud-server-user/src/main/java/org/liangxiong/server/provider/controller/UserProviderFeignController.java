package org.liangxiong.server.provider.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:16:04
 * @Description 用户服务提供方(Feign方式)
 */
@Slf4j
@RestController
public class UserProviderFeignController implements IUserService {

    private static final Random RANDOM = new Random();

    @Autowired
    @Qualifier("inMemoryUserServiceImpl")
    private IUserService userService;

    /**
     * @param user 输入参数;path对应服务端POST:/users
     * @return
     */
    @Override
    public boolean addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    /**
     * path对应服务端GET:/users
     *
     * @return 所有用户列表
     */
    @Override
    public List<User> listAllUsers() {
        return userService.listAllUsers();
    }

    /**
     * 通过id获取指定用户
     *
     * @param userId
     * @return
     */
    @HystrixCommand(commandProperties = @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "300"))
    @Override
    public User getUserById(Integer userId) {
        try {
            int time = RANDOM.nextInt(500);
            log.info("sleep time: {}", time);
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            log.error("method execution interrupt!");
        }
        return userService.getUserById(userId);
    }

}
