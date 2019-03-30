package org.liangxiong.stream.binder.activemq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;

import javax.jms.*;

/**
 * @author liangxiong
 * @Date:2019-03-29
 * @Time:9:41
 * @Description ActiveMQ的Stream Cloud Binder实现
 */
@Slf4j
public class ActiveMessageChannelBinder implements Binder<MessageChannel, ConsumerProperties, ProducerProperties> {

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 接收ActiveMQ的消息
     *
     * @param name
     * @param group
     * @param messageChannel
     * @param consumerProperties
     * @return
     */
    @Override
    public Binding<MessageChannel> bindConsumer(String name, String group, MessageChannel messageChannel, ConsumerProperties consumerProperties) {
        // 生产环境中应该使用连接池操作
        ConnectionFactory factory = jmsTemplate.getConnectionFactory();
        try {
            Connection connection = factory.createConnection();
            // 启动连接
            connection.start();
            // 创建会话
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // 创建目的地
            Destination destination = new ActiveMQQueue(name);
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(message -> {
                // 注意:回调函数监听消息,如果此时connection关闭,则消息监听失败!
                if (message instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    try {
                        Object data = objectMessage.getObject();
                        if (log.isInfoEnabled()) {
                            log.info("receive message from activemq binder");
                        }
                        // 发送给input管道
                        messageChannel.send(new GenericMessage<>(data));
                    } catch (JMSException e) {
                        log.error("get object from message error: {}", e.getMessage());
                    }
                }
            });
        } catch (JMSException e) {
            log.error("consumer execution error: {}", e.getMessage());
        }
        return () -> log.info("consume message...");
    }

    /**
     * 发送消息到ActiveMQ
     *
     * @param name
     * @param messageChannel
     * @param producerProperties
     * @return
     */
    @Override
    public Binding<MessageChannel> bindProducer(String name, MessageChannel messageChannel, ProducerProperties producerProperties) {
        // 判断MessageChannel的类型
        Assert.isInstanceOf(SubscribableChannel.class, messageChannel, "MessageChannel must be SubscribableChannel!");
        SubscribableChannel subscribableChannel = (SubscribableChannel) messageChannel;
        subscribableChannel.subscribe(message -> {
            Object payload = message.getPayload();
            if (log.isInfoEnabled()) {
                log.info("send message from activemq binder");
            }
            // 消息发送到output管道
            jmsTemplate.convertAndSend(name, payload);
        });
        return () -> log.info("produce message...");
    }
}
