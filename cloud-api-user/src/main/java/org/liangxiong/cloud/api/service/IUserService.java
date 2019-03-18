package org.liangxiong.cloud.api.service;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.fallback.UserServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:9:57
 * @Description 用户操作业务层
 */
@FeignClient(value = "${provider.user.service.name}", fallback = UserServiceFallback.class)
public interface IUserService {

    /**
     * 添加用户,feign指定请求路径
     *
     * @param user
     * @return
     */
    @PostMapping("/feign/users")
    boolean addUser(User user);

    /**
     * 获取所有地用户
     *
     * @return
     */
    @GetMapping("/feign/users")
    List<User> listAllUsers();

    /**
     * 通过id获取指定用户(@RequestParam解决客户端请求报错)
     *
     * @param userId
     * @return
     */
    @GetMapping("/feign/users/id")
    User getUserById(@RequestParam("userId") Integer userId);
}
