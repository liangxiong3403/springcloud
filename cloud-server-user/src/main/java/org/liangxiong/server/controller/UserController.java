package org.liangxiong.server.controller;

import com.alibaba.fastjson.JSONObject;
import org.liangxiong.server.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 添加用户
     *
     * @param user 用户实体
     * @return
     */
    @PostMapping
    public JSONObject addUser(@RequestBody User user) {
        JSONObject result = new JSONObject(8);
        result.put("name", user.getUsername());
        result.put("age", user.getAge());
        // 区分服务端
        result.put("serverPort", port);
        return result;
    }
}
