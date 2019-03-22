package org.liangxiong.ribbon.controller;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.ribbon.stream.UserMessageStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

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

    private User user;

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
     * 监听消息
     *
     * @return
     */
    @StreamListener(UserMessageStream.INPUT)
    public void listenerMessage(byte[] data) {
        deserializeObject(data);
        System.err.println("user: " + user.getUsername());
        //        userMessageStream.input().subscribe(message -> deserializeObject((byte[]) data.getPayload()));
    }

    /**
     * 对字节数组进行反序列化
     *
     * @param source
     */
    private void deserializeObject(byte[] source) {
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(source);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                Object message = ois.readObject();
                if (message instanceof User) {
                    user = (User) message;
                }
            }
        } catch (Exception e) {
            log.error("Deserialize execution error!");
        }
    }

}
