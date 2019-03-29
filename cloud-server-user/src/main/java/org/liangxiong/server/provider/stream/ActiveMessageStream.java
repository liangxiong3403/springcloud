package org.liangxiong.server.provider.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

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
    String OUTPUT = "active-message";

    /**
     * 发送MQ消息
     *
     * @return
     */
    @Output(OUTPUT)
    MessageChannel output();
}
