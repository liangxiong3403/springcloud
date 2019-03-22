package org.liangxiong.ribbon.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:15:03
 * @Description
 */
public interface UserMessageStream {

    /**
     * 管道名称
     */
    String INPUT = "user-message";

    /**
     * 接收MQ的消息
     *
     * @return
     */
    @Input(INPUT)
    SubscribableChannel input();
}
