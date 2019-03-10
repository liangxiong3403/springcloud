package org.liangxiong.server.provider.controller;

import com.alibaba.fastjson.JSONObject;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:06
 * @Description 用户服务provider
 */
@RequestMapping("/users")
@RestController
public class UserController {

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private IUserService userService;

    /**
     * 添加用户
     *
     * @param user 用户实体
     * @return
     */
    @PostMapping
    public Object addUser(@RequestBody User user) {
        JSONObject result = new JSONObject(8);
        result.put("name", user.getUsername());
        result.put("age", user.getAge());
        result.put("userId", user.getUserId());
        // 区分服务端
        result.put("serverPort", port);
        return userService.addUser(user) ? result : new HashMap<Integer, Object>(8);
    }

    /**
     * 获取所有用户数据
     *
     * @return
     */
    @GetMapping
    public List<User> listAllUser() {
        return userService.listAllUsers();
    }

}
