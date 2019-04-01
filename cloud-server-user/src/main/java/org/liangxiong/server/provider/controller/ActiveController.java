package org.liangxiong.server.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.server.provider.stream.ActiveMessageStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;

/**
 * @author liangxiong
 * @Date:2019-03-28
 * @Time:14:53
 * @Description ActiveMQ发送消息
 */
@Slf4j
@RequestMapping("/activemq")
@RestController
public class ActiveController {

    @Value("${spring.activemq.broker-url:tcp://localhost:61616}")
    private String brokerUrl;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMessageStream activeMessageStream;

    /**
     * 原生api发送消息
     *
     * @param queueName 队列名称
     * @param user      消息内容
     */
    @PostMapping("/message/primitive")
    public boolean sendMessagePrimitive(@RequestParam String queueName, @RequestBody User user) {
        // 构造连接工厂
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            // 获取连接
            connection = factory.createConnection();
            // 创建会话(非transacted,自动确认)
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // 创建目的地
            Destination destination = new ActiveMQQueue(queueName);
            // 创建生产者
            producer = session.createProducer(destination);
            // 构造消息
            ActiveMQObjectMessage message = new ActiveMQObjectMessage();
            message.setObject(user);
            // 发送消息
            producer.send(message);
            return true;
        } catch (JMSException e) {
            log.error("connection create error: {}", e.getMessage());
        } finally {
            if (null != producer) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    log.error("producer close exception: {}", e.getMessage());
                }
            }
            if (null != session) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("session close exception: {}", e.getMessage());
                }
            }
            if (null != connection) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.error("connection close exception: {}", e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * 封装方式发送消息
     *
     * @param queueName 队列名称
     * @param user      消息内容
     */
    @PostMapping("/message/advanced")
    public boolean sendMessageAdvanced(String queueName, @RequestBody User user) {
        if (StringUtils.hasText(queueName)) {
            Destination destination = new ActiveMQQueue(queueName);
            jmsTemplate.convertAndSend(destination, user);
        } else {
            jmsTemplate.convertAndSend(user);
        }
        return true;
    }

    /**
     * 封装方式发送消息
     *
     * @param user 消息内容
     */
    @PostMapping("/message/binder")
    public boolean sendMessageBinder(@RequestBody User user) {
        return activeMessageStream.output().send(new GenericMessage<>(user));
    }
}
