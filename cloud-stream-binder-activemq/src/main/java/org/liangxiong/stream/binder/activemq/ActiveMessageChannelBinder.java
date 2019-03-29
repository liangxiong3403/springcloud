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
        ConnectionFactory factory = jmsTemplate.getConnectionFactory();
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            connection = factory.createConnection();
            // 启动连接
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = new ActiveMQQueue(name);
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(message -> {
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
        } finally {
            if (null != consumer) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    log.error("consumer cloud error: {}", e.getMessage());
                }
            }
            if (null != session) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("session cloud error: {}", e.getMessage());
                }
            }
            if (null != connection) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.error("connection cloud error: {}", e.getMessage());
                }
            }
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
            jmsTemplate.convertAndSend(payload);
        });
        return () -> log.info("produce message...");
    }
}
