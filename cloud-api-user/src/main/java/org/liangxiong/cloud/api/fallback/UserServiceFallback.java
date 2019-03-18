package org.liangxiong.cloud.api.fallback;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:21:51
 * @Description {@link IUserService}回调实现类, 用于feign的断路器配置
 */
@Component
public class UserServiceFallback implements IUserService {

    @Override
    public boolean addUser(User user) {
        return false;
    }

    @Override
    public List<User> listAllUsers() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public User getUserById(Integer userId) {
        return null;
    }
}
