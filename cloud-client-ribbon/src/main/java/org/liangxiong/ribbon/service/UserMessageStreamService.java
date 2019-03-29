package org.liangxiong.ribbon.service;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.ribbon.stream.UserMessageStream;
import org.liangxiong.ribbon.util.UserConsumerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            User user = UserConsumerUtil.getUserFromPayload(source);
            if (log.isInfoEnabled()) {
                log.info("receive message from SubscribableChannel: {}", user.getUsername());
            }
        });
    }

    //@ServiceActivator(inputChannel = UserMessageStream.INPUT)
    public void receiveMessage(Object source) {
        User user = UserConsumerUtil.getUserFromPayload(source);
        if (log.isInfoEnabled()) {
            log.info("receive message from ServiceActivator: {}", user.getUsername());
        }
    }

}
