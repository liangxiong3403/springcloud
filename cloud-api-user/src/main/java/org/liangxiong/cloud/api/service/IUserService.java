package org.liangxiong.cloud.api.service;

import org.liangxiong.cloud.api.domain.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:9:57
 * @Description 用户操作业务层
 */
@FeignClient("${provider.user.service.name}")
public interface IUserService {

    /**
     * 添加用户,feign指定请求路径
     *
     * @param user
     * @return
     */
    @PostMapping("/users")
    Boolean addUser(User user);

    /**
     * 获取所有地用户,feign指定请求路径
     *
     * @return
     */
    @GetMapping("/users")
    List<User> listAllUsers();
}
