package org.liangxiong.ribbon.controller;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.ribbon.stream.UserMessageStream;
import org.liangxiong.ribbon.util.UserDeserializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:13:37
 * @Description 消息处理
 */
@Slf4j
@RequestMapping("/kafka")
@RestController
public class MessageController {

    @Autowired
    private UserMessageStream userMessageStream;

    private User user = new User();

    /**
     * 获取消息
     *
     * @return
     */
    @GetMapping("/message/object")
    public User returnMessage() {
        return user;
    }

    /**
     * 监听消息方式一
     *
     * @return
     */
    @StreamListener(UserMessageStream.INPUT)
    public void listenerMessage(Object source) {
        if (source.getClass().isArray()) {
            // 消息为二进制
            user = UserDeserializeUtil.deserializeObject((byte[]) source);
        } else if (source instanceof User) {
            // 消息为原始对象(未被序列化)
            user = (User) source;
        }
        if (log.isInfoEnabled()) {
            log.info("receive message from StreamListener: {}", user.getUsername());
        }
    }

}
