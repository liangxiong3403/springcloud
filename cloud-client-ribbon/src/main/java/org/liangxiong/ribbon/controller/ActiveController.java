package org.liangxiong.ribbon.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.ribbon.stream.ActiveMessageStream;
import org.liangxiong.ribbon.util.UserConsumerUtil;
import org.liangxiong.ribbon.util.UserDeserializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.*;

/**
 * @author liangxiong
 * @Date:2019-03-28
 * @Time:14:53
 * @Description ActiveMQ接收消息
 */
@Slf4j
@RequestMapping("/activemq")
@RestController
public class ActiveController {

    @Value("${spring.activemq.broker-url:tcp://localhost:61616}")
    private String brokerUrl;

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 原生api接收消息
     *
     * @param queueName 队列名称
     */
    @GetMapping("/message/primitive")
    public Object receiveMessagePrimitive(@RequestParam String queueName) {
        // 构造连接工厂
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        Object data = null;
        try {
            // 获取连接
            connection = factory.createConnection();
            // 启动连接(如果没有这一步,则无法接收到消息)
            connection.start();
            // 创建会话(非transacted,自动确认)
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // 创建目的地
            Destination destination = new ActiveMQQueue(queueName);
            // 创建消费者
            consumer = session.createConsumer(destination);
            // 接收消息
            Message message = consumer.receive(200);
            if (message instanceof ActiveMQObjectMessage) {
                ActiveMQObjectMessage objectMessage = (ActiveMQObjectMessage) message;
                data = UserDeserializeUtil.deserializeObject(objectMessage.getContent().data);
                log.info("receive message from activemq: {}", data);
            }
        } catch (JMSException e) {
            log.error("connection create error: {}", e.getMessage());
        } finally {
            if (null != consumer) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    log.error("consumer close exception: {}", e.getMessage());
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
        return data;
    }

    /**
     * 封装方式发接收消息
     *
     * @param queueName 队列名称
     */
    @GetMapping("/message/advanced")
    public Object receiveMessageAdvanced(String queueName) {
        jmsTemplate.setReceiveTimeout(1000);
        if (StringUtils.hasText(queueName)) {
            Destination destination = new ActiveMQQueue(queueName);
            return jmsTemplate.receiveAndConvert(destination);
        }
        return jmsTemplate.receiveAndConvert();
    }

    /**
     * 通过Cloud Stream Binder获取消息
     *
     * @param source
     */
    @StreamListener(ActiveMessageStream.INPUT)
    public void receiveMessageFromChannel(Object source) {
        User user = UserConsumerUtil.getUserFromPayload(source);
        if (log.isInfoEnabled()) {
            log.info("receive message from StreamListener: {}", user.getUsername());
        }
    }
}
