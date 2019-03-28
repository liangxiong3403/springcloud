# ActiveMQ原生方式

## 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
```

## 配置MQ地址

```yaml
spring:
    activemq:
        broker-url: tcp://localhost:61616
```

## 发送消息

```java
package org.liangxiong.server.provider.controller;

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
}
```

## 接收消息

```java
package org.liangxiong.ribbon.controller;

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

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    /**
     * 原生api接收消息
     *
     * @param queueName 队列名称
     */
    @GetMapping("/message/primitive")
    public String receiveMessagePrimitive(@RequestParam String queueName) {
        // 构造连接工厂
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        String text = null;
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
            if (message instanceof ActiveMQTextMessage) {
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                text = textMessage.getText();
                log.info("receive message from activemq: {}", text);
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
        return text;
    }
}
```

# Spring Boot方式

## 发送消息

```java
package org.liangxiong.server.provider.controller;

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
```

## 接收消息

```java
package org.liangxiong.ribbon.controller;

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

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 封装方式发接收消息
     *
     * @param queueName 队列名称
     */
    @GetMapping("/message/advanced")
    public String receiveMessageAdvanced(@RequestParam String queueName) {
        Destination destination = new ActiveMQQueue(queueName);
        Message message = jmsTemplate.receive(destination);
        String text = null;
        if (message instanceof ActiveMQTextMessage) {
            ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
            try {
                text = textMessage.getText();
                log.info("receive message from activemq: {}", text);
            } catch (JMSException e) {
                log.error("connection close exception: {}", e.getMessage());
            }
        }
        return text;
    }
}
```

# 自定义实现ActiveMQ的Cloud Stream Binder实现

## 创建项目`cloud-stream-binder-activemq`