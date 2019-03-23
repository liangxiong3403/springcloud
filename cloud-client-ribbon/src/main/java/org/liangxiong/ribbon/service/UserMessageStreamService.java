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
            User user = UserDeserializeUtil.deserializeObject((byte[]) message.getPayload());
            if (log.isInfoEnabled()) {
                log.info("receive message from SubscribableChannel: {}", user.getUsername());
            }
        });
    }

    //@ServiceActivator(inputChannel = UserMessageStream.INPUT)
    public void receiveMessage(byte[] data) {
        User user = UserDeserializeUtil.deserializeObject(data);
        if (log.isInfoEnabled()) {
            log.info("receive message from ServiceActivator: {}", user.getUsername());
        }
    }
}
