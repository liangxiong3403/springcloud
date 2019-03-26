package org.liangxiong.ribbon.service;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.ribbon.stream.UserMessageStream;
import org.liangxiong.ribbon.util.UserDeserializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author liangxiong
 * @Date:2019-03-23
 * @Time:18:24
 * @Description 使用SubscribableChannel的方式接受消息
 */
@Slf4j
@Service
public class UserMessageStreamService {

    @Autowired
    private UserMessageStream userMessageStream;

    //@PostConstruct
    public void init() {
        userMessageStream.input().subscribe(message -> {
            Object source = message.getPayload();
            User user = getUserFromPayload(source);
            if (log.isInfoEnabled()) {
                log.info("receive message from SubscribableChannel: {}", user.getUsername());
            }
        });
    }

    //@ServiceActivator(inputChannel = UserMessageStream.INPUT)
    public void receiveMessage(Object source) {
        User user = getUserFromPayload(source);
        if (log.isInfoEnabled()) {
            log.info("receive message from ServiceActivator: {}", user.getUsername());
        }
    }

    private User getUserFromPayload(Object source) {
        User user = new User();
        // 注意:需要判断消息类型
        if (source.getClass().isArray()) {
            // 消息为二进制
            user = UserDeserializeUtil.deserializeObject((byte[]) source);
        } else if (source instanceof User) {
            // 消息为原始对象(未被序列化)
            user = (User) source;
        }
        return user;
    }
}
