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

## 引入依赖

```xml
<!-- Cloud Stream相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
<!-- ActiveMQ的依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
```

## 自定义Binder实现类

```java
package org.liangxiong.stream.binder.activemq;
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
        // 消息发送到output管道
        subscribableChannel.subscribe(message -> {
            Object payload = message.getPayload();
            jmsTemplate.convertAndSend(payload);
        });
        return () -> log.info("produce message...");
    }
}
```

## 装配Binder实现类

```java
package org.liangxiong.stream.binder.activemq.config;

/**
 * @author liangxiong
 * @Date:2019-03-29
 * @Time:10:00
 * @Description 自动装配ActiveMessageChannelBinder的bean
 */
@ConditionalOnMissingBean(ActiveMessageChannelBinder.class)
@Configuration
public class ActiveBinderConfiguration {

    @Bean
    ActiveMessageChannelBinder activeMessageChannelBinder() {
        return new ActiveMessageChannelBinder();
    }
}
```

## 配置Binder的定义(META-INF/spring.binders)

```properties
activemq: org.liangxiong.stream.binder.activemq.config.ActiveBinderConfiguration
```

## 项目`cloud-stream-binder-activemq`安装到本地Maven仓库

## 其他项目引入`cloud-stream-binder-activemq`

- `cloud-server-user`引入依赖

  ```xml
  <!-- 自定义ActiveMQ的Binder实现 -->
  <dependency>
      <groupId>org.liangxiong</groupId>
      <artifactId>cloud-stream-binder-activemq</artifactId>
  </dependency>
  ```

- `cloud-client-ribbon`引入依赖

  ```xml
  <!-- 自定义ActiveMQ的Binder实现 -->
  <dependency>
      <groupId>org.liangxiong</groupId>
      <artifactId>cloud-stream-binder-activemq</artifactId>
  </dependency>
  ```

## 配置生产者`cloud-server-user`

```yaml
server:
    port: 8090
management:
    port: 9011
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-user-server
    cloud:
        stream:
            # 激活Spring Cloud Stream Binding(激活@StreamListener)
            bindings:
                active-message:
                    destination: test
                    # 名称来自于META-INF/spring.factories
                    binder: activemq
    rabbitmq:
        addresses: 192.168.0.130,192.168.0.131,192.168.0.132
        username: admin
        password: 123456
    activemq:
        broker-url: tcp://localhost:61616
    jms:
        template:
            default-destination: test
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

    @Autowired
    private ActiveMessageStream activeMessageStream;

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
```

## 配置消费者

```yaml
server:
    port: 8089
management:
    port: 9010
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-ribbon-client
    cloud:
        circuit:
            breaker:
                # 控制@EnableCircuitBreaker的开关方式一
                enabled: true
        stream:
            # 激活Spring Cloud Stream Binding(激活@StreamListener)
            bindings:
                active-message:
                    destination: test
                    # 名称来自于META-INF/spring.factories
                    binder: activemq
	activemq:
		broker-url: tcp://localhost:61616
		packages:
			trust-all: true
    jms:
        template:
            default-destination: test
```

## 消费消息

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

    @Autowired
    private JmsTemplate jmsTemplate;

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
```

