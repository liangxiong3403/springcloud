# Spring Cloud Stream

> Spring Cloud Stream is a framework for building message-driven microservice applications. Spring Cloud Stream builds upon Spring Boot to create standalone, production-grade Spring applications, and uses Spring Integration to provide connectivity to message brokers. It provides opinionated configuration of middleware from several vendors, introducing the concepts of persistent publish-subscribe semantics, consumer groups, and partitions.

# 项目集成Spring Cloud Stream

## 修改项目`cloud-api-user`的API

- 项目`cloud-api-user`调整实体

  ```java
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:11:07
   * @Description
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public class User implements Serializable {
  
      private static final long serialVersionUID = -3375730742325922962L;
  
      /**
       * 用户id
       */
      private Integer userId;
  
      /**
       * 用户名
       */
      private String username;
  
      /**
       * 年龄
       */
      private Integer age;
  
  }
  ```

## 调整项目`cloud-server-user`为消息生产者

- 项目添加依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-kafka</artifactId>
  </dependency>
  ```

- 配置对象序列化器

  ```java
  package org.liangxiong.server.provider.component;
  
  /**
   * @author liangxiong
   * @Date:2019-03-22
   * @Time:11:19
   * @Description 用于broker的对象序列化
   */
  @Slf4j
  public class ObjectSerializer implements Serializer<Object> {
  
      @Override
      public void configure(Map<String, ?> map, boolean b) {
  
      }
  
      /**
       * @param topic 消息主题
       * @param data  消息内容
       * @return
       */
      @Override
      public byte[] serialize(String topic, Object data) {
          byte[] result = new byte[0];
          try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
               ObjectOutputStream oos = new ObjectOutputStream(bos)) {
              oos.writeObject(data);
              // 获取序列化以后的字节数组
              result = bos.toByteArray();
          } catch (Exception e) {
              log.error("Serializer execution error: {}", e.getMessage());
          }
          return result;
      }
  
      @Override
      public void close() {
  
      }
  }
  ```

- 配置Spring Boot配置文件

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
      kafka:
          # 集群地址
          bootstrap-servers: 192.168.0.130:9092,192.168.0.131:9092,192.168.0.132:9092
          producer:
              acks: 1
              client-id: lx-spring-cloud-stream-producer1
              key-serializer: org.apache.kafka.common.serialization.StringSerializer
              value-serializer: org.liangxiong.server.provider.component.ObjectSerializer
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
          # 引入eureka客户端依赖后,临时关闭eureka客户端功能
          #enabled: false
  ```

- 生产者发送消息

  ```java
  package org.liangxiong.server.provider.controller;
  
  /**
   * @author liangxiong
   * @Date:2019-03-22
   * @Time:13:37
   * @Description 消息处理
   */
  @Slf4j
  @RequestMapping("/kafka")
  @RestController
  public class MessageController {
  
      private KafkaTemplate<String, Object> kafkaTemplate;
  
      @Autowired
      public MessageController(KafkaTemplate kafkaTemplate) {
          this.kafkaTemplate = kafkaTemplate;
      }
  
      /**
       * 发送消息
       *
       * @param topic 消息主题
       * @param key   关键字
       * @param user  消息内容
       * @return
       */
      @PostMapping("/message/object")
      public JSONObject sendMessage(@RequestParam String topic, @RequestParam String key, @RequestBody User user) {
          ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(topic, key, user);
          JSONObject result = new JSONObject(4);
          try {
              SendResult<String, Object> sendResult = listenableFuture.get(3, TimeUnit.SECONDS);
              result.put("partition", sendResult.getRecordMetadata().partition());
              result.put("timestamp", sendResult.getRecordMetadata().timestamp());
          } catch (InterruptedException e) {
              log.error("线程被中断");
          } catch (ExecutionException e) {
              log.error("任务执行异常");
          } catch (TimeoutException e) {
              log.error("任务执行超时");
          }
          return result;
      }
  }
  ```

## 调整项目`cloud-client-ribbon`为消息消费者

- 项目添加依赖

  ```xml
  <!-- Stream Kafka相关依赖 -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-kafka</artifactId>
  </dependency>
  ```

- 修改配置文件

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
              kafka:
                  binder:
                      # 默认是本机MQ地址
                      brokers: 192.168.0.130,192.168.0.131,192.168.0.132
                      # 默认是本机zookeeper地址
                      zkNodes: 192.168.0.130,192.168.0.131,192.168.0.132
              # 激活Spring Cloud Stream Binding(激活@StreamListener)
              bindings:
                  # 名称user-message-kafka来自于org.liangxiong.ribbon.stream.UserMessageStream.INPUT的值
                  user-message-kafka:
                      # 配置topic
                      destination: test
  ```

- 消费者启动类

  ```java
  package org.liangxiong.ribbon;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:9:50
   * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端,Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
   * IUserService作为feign的客户端接口
   */
  @EnableBinding(UserMessageStream.class)
  @SpringBootApplication
  public class RibbonClientApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(RibbonClientApplication.class, args);
      }
  }
  ```

- 定义`Bindable interface with one input channel`

  ```java
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
      String INPUT = "user-message-kafka";
  
      /**
       * 接收MQ的消息
       *
       * @return
       */
      @Input(INPUT)
      SubscribableChannel input();
  }
  ```

- 客户端激活管道

  ```java
  package org.liangxiong.ribbon;
  
  @EnableBinding(UserMessageStream.class)
  @SpringBootApplication
  public class RibbonClientApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(RibbonClientApplication.class, args);
      }
  }
  ```

## 消费消息的三种方式(三选一)

- 消费端**第一种**写法

  ```java
  package org.liangxiong.ribbon.controller;
  
  /**
   * @author liangxiong
   * @Date:2019-03-22
   * @Time:13:37
   * @Description 消息处理
   */
  @Slf4j
  @RequestMapping("/kafka")
  @RestController
  public class MessageController {
  
      @Autowired
      private UserMessageStream userMessageStream;
  
      private User user;
  
      /**
       * 获取消息
       *
       * @return
       */
      @GetMapping("/message/object")
      public User returnMessage() {
          return user;
      }
  
      /**
       * 监听消息方式一
       *
       * @return
       */
      @StreamListener(UserMessageStream.INPUT)
      public void listenerMessage(byte[] data) {
          user = UserDeserializeUtil.deserializeObject(data);
          if (log.isInfoEnabled()) {
              log.info("receive message from StreamListener: {}", user.getUsername());
          }
      }
  
  }
  ```

- 消费端**第二种**写法

  ```java
  package org.liangxiong.ribbon.service;
  
  /**
   * @author liangxiong
   * @Date:2019-03-23
   * @Time:18:24
   * @Description 使用SubscribableChannel的方式接受消息
   */
  @Slf4j
  @Service
  public class UserMessageStreamService {
  
      @Autowired
      private UserMessageStream userMessageStream;
  
      @PostConstruct
      public void init() {
          userMessageStream.input().subscribe(message -> {
              User user = UserDeserializeUtil.deserializeObject((byte[]) message.getPayload());
              if (log.isInfoEnabled()) {
                  log.info("receive message from SubscribableChannel: {}", user.getUsername());
              }
          });
      }
  }
  ```

- 消费端**第三种**写法

  ```java
  package org.liangxiong.ribbon.service;
  
  /**
   * @author liangxiong
   * @Date:2019-03-23
   * @Time:18:24
   * @Description 使用SubscribableChannel的方式接受消息
   */
  @Slf4j
  @Service
  public class UserMessageStreamService {
  
      @ServiceActivator(inputChannel = UserMessageStream.INPUT)
      public void receiveMessage(byte[] data) {
          User user = UserDeserializeUtil.deserializeObject(data);
          if (log.isInfoEnabled()) {
              log.info("receive message from ServiceActivator: {}", user.getUsername());
          }
      }
  }
  ```

## 通过命令行查看消息(调试)

> sh ./bin/kafka-console-consumer.sh --bootstrap-server 192.168.0.131:9092 --topic test --from-beginningworld

# 注意事项

## 主机名称约束

- 如果服务器配置如下

  ```properties
  127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
  ::1         localhost localhost.localdomain localhost6 localhost6.localdomain6
  192.168.43.2 node1
  192.168.43.3 node2
  192.168.43.4 node3
  192.168.43.5 node4
  ```

- 开发环境也必须配置hosts(比如配置window或者mac的hosts文件)

  ```tex
  如果开发环境不配置,则无法调试
  ```

## Kafka配置leader(集群中`只有一台`服务器需要配置)

- `im kafka/config/server.properties `(`node1`机器配置)

- 修改server.properties

  ```
  advertised.listeners=PLAINTEXT://node1:9092
  ```

## 客户端@StreamListener报错

- 报错信息一

  ```tex
  java.lang.IllegalArgumentException: A method annotated with @StreamListener having a return type should also have an outbound target specified
  ```

  - 报错原因

    ```tex
    @StreamListener标记的方法不能有返回值
    ```

  - 修复

    ```java
    @StreamListener(UserMessageStream.INPUT)
    public void listenerMessage(byte[] data) {
        deserializeObject(data);
    }
    ```

- 报错信息二

  > 2019-03-22 17:14:51.480 | ERROR | -L-5 | org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder | Could not convert message: ACED0005737200246F72672E6C69616E6778696F6E672E636C6F75642E6170692E646F6D61696E2E55736572D126FEC6764C336E0200034C00036167657400134C6A6176612F6C616E672F496E74656765723B4C000675736572496471007E00014C0008757365726E616D657400124C6A6176612F6C616E672F537472696E673B7870737200116A6176612E6C616E672E496E746567657212E2A0A4F781873802000149000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B0200007870000000127371007E000400000004740009E78E8BE78699E587A4
  > java.lang.StringIndexOutOfBoundsException: String index out of range: -19

  - 报错原因

    ```tex
    by default the Spring Cloud Stream Kafka Binder will expect headers to be present
    ```

  - 官方说明

    ```tex
    https://github.com/spring-cloud/spring-cloud-stream-binder-kafka/issues/410
    ```

  - 解决报错

    ```yaml
    spring:
        application:
            name: spring-cloud-ribbon-client
        cloud:
            circuit:
                breaker:
                    # 控制@EnableCircuitBreaker的开关方式一
                    enabled: true
            stream:
                kafka:
                    binder:
                        # 默认是本机MQ地址
                        brokers: 192.168.43.2,192.168.43.3,192.168.43.4
                        # 默认是本机zookeeper地址
                        zkNodes: 192.168.43.2,192.168.43.3,192.168.43.4
                # 激活Spring Cloud Stream Binding(激活@StreamListener)
                bindings:
                    # 名称user-message来自于org.liangxiong.ribbon.stream.UserMessageStream.INPUT的值
                    user-message:
                        # 配置topic
                        destination: test
                        # 解决客户端消费消息时报错:org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder | Could not convert message:
                        # you need to specify headerMode: raw with spring-cloud-streaam 1.3.x. It is not needed in 2.0.x.
                        consumer:
                            # 解决报错
                            header-mode: raw
    ```


## 如果同时使用三种消息接收方式

> 则会交替接收消息:某一时刻,只有一种方式会收到消息

# 集成RabbitMQ

## 项目`cloud-client-ribbon`引入依赖

```xml
<!-- Stream Rabbit相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

## `cloud-server-user`引入依赖

```xml
<!-- Stream Rabbit相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

## `UserMessageStream`抽取到项目`cloud-api-user`中,形成公共API

- 提取接口

```java
package org.liangxiong.cloud.api.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:15:03
 * @Description 消息发送接口
 */
public interface UserMessageStream {

    /**
     * 管道名称
     */
    String CHANNEL_NAME = "user-message";

    /**
     * 接收MQ的消息
     *
     * @return
     */
    @Input(CHANNEL_NAME)
    SubscribableChannel input();

    /**
     * 发送MQ消息
     *
     * @return
     */
    @Output(CHANNEL_NAME)
    MessageChannel output();
}
```

- 项目`cloud-api-user`引入依赖

  ```xml
  <!-- Spring Cloud Stream相关依赖 -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-stream</artifactId>
  </dependency>
  ```

- 项目`cloud-client-ribbon`和`cloud-server-user`已经通过引入`cloud-api-user`间接引入了`UserMessageStream`

## 客户端启动报错

- 报错信息

  ```tex
  Caused by: java.lang.IllegalStateException: A default binder has been requested, but there is more than one binder available for 'org.springframework.integration.channel.DirectChannel' : kafka,rabbit, and no default binder has been set.
  	at org.springframework.cloud.stream.binder.DefaultBinderFactory.getBinder(DefaultBinderFactory.java:133)
  ```

- 报错原因

  ```tex
  因为同时引入了spring-cloud-starter-stream-kafka和spring-cloud-starter-stream-rabbit
  ```

- 解决方法(设置默认binder)

  ```yaml
  spring:
      application:
          name: spring-cloud-ribbon-client
      cloud:
          stream:
          	# 设置默认binder
              default-binder: rabbit
  ```

## 客户端`cloud-client-ribbon`发送消息

```java
package org.liangxiong.server.provider.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.server.provider.stream.UserMessageStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:13:37
 * @Description 消息处理
 */
@Slf4j
@RequestMapping("/kafka")
@RestController
public class MessageController {

    @Autowired
    private UserMessageStream userMessageStream;

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public MessageController(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 原生方式发送消息
     *
     * @param topic 消息主题
     * @param key   关键字
     * @param user  消息内容
     * @return
     */
    @PostMapping("/message/object/primitive")
    public JSONObject sendMessagePrimitively(@RequestParam String topic, @RequestParam String key, @RequestBody User user) {
        ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(topic, key, user);
        JSONObject result = new JSONObject(4);
        try {
            SendResult<String, Object> sendResult = listenableFuture.get(3, TimeUnit.SECONDS);
            result.put("partition", sendResult.getRecordMetadata().partition());
            result.put("timestamp", sendResult.getRecordMetadata().timestamp());
        } catch (InterruptedException e) {
            log.error("线程被中断");
        } catch (ExecutionException e) {
            log.error("任务执行异常");
        } catch (TimeoutException e) {
            log.error("任务执行超时");
        }
        return result;
    }

    /**
     * Stream 的方式发送消息
     *
     * @param user
     * @return
     */
    @PostMapping("/message/object/stream")
    public boolean sendMessage(@RequestBody User user) {
        MessageChannel messageChannel = userMessageStream.output();
        Message<String> message = new GenericMessage(JSON.toJSONString(user));
        return messageChannel.send(message, 3000);
    }
}
```

