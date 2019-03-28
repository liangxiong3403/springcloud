package org.liangxiong.server.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 原生api发送消息
     *
     * @param queueName 队列名称
     * @param content   消息内容
     */
    @PostMapping("/message/primitive")
    public void sendMessagePrimitive(@RequestParam String queueName, @RequestParam String content) {
        // 构造连接工厂
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
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
            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(content);
            // 发送消息
            producer.send(message);
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
    }

    /**
     * 封装方式发送消息
     *
     * @param queueName 队列名称
     * @param content   消息内容
     */
    @PostMapping("/message/advanced")
    public void sendMessageAdvanced(@RequestParam String queueName, @RequestParam String content) {
        Destination destination = new ActiveMQQueue(queueName);
        jmsTemplate.send(destination, e -> e.createTextMessage(content));
    }
}
