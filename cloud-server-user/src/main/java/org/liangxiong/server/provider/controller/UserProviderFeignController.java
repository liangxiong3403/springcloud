package org.liangxiong.server.provider.controller;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:16:04
 * @Description 用户服务提供方(Feign方式)
 */
@RequestMapping("/diy/feign/server")
@RestController
public class UserProviderFeignController implements IUserService {

    @Autowired
    @Qualifier("inMemoryUserServiceImpl")
    private IUserService userService;

    /**
     * @param user 输入参数;path对应服务端POST:/users
     * @return
     */
    @Override
    public Boolean addUser(@RequestBody User user) {
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
}
