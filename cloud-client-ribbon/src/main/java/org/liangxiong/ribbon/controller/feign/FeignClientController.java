package org.liangxiong.ribbon.controller.feign;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:15:48
 * @Description Feign的方式调用远程服务;注意:官方不推荐客户端和服务端同时实现feign客户端接口(比如IUserService)
 */
@RequestMapping("/diy/feign/client")
@RestController
public class FeignClientController implements IUserService {

    /**
     * 为报错是因为@FeignClient mark the feign proxy as a primary bean
     */
    @Autowired
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
