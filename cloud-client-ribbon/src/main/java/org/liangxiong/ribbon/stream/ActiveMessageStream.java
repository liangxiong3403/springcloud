package org.liangxiong.ribbon.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author liangxiong
 * @Date:2019-03-29
 * @Time:10:34
 * @Description 处理ActiveMQ的管道消息, 测试自定义Active相关Binder实现
 */
public interface ActiveMessageStream {

    /**
     * 输出管道名称
     */
    String INPUT = "active-message";

    /**
     * 接收MQ消息
     *
     * @return
     */
    @Input(INPUT)
    SubscribableChannel input();
}
