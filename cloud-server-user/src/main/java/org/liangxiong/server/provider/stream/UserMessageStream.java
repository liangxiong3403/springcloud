package org.liangxiong.server.provider.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:15:03
 * @Description 消息发送接口
 */
public interface UserMessageStream {

    /**
     * 输出管道名称
     */
    String OUTPUT = "user-message";

    /**
     * 发送MQ消息
     *
     * @return
     */
    @Output(OUTPUT)
    MessageChannel output();
}
