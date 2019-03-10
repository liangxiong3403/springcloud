package org.liangxiong.cloud.api.service;

import org.liangxiong.cloud.api.domain.User;

import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:9:57
 * @Description 用户操作业务层
 */
public interface IUserService {

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    boolean addUser(User user);

    /**
     * 获取所有地用户
     *
     * @return
     */
    List<User> listAllUsers();
}
