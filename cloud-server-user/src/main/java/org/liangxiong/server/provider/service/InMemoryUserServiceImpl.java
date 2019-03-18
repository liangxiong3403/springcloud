package org.liangxiong.server.provider.service;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:10:02
 * @Description 内存中地实现, 没有访问持久层;集成Feign时,指定bean名称
 */
@Service("inMemoryUserServiceImpl")
public class InMemoryUserServiceImpl implements IUserService {

    private Map<Integer, User> repository = new ConcurrentHashMap<>(8);

    @Override
    public Boolean addUser(User user) {
        return repository.put(user.getUserId(), user) == null;
    }

    @Override
    public List<User> listAllUsers() {
        return new ArrayList<>(repository.values());
    }
}
