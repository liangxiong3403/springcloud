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
                  # 名称user-message来自于org.liangxiong.ribbon.stream.UserMessageStream.INPUT的值
                  user-message:
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
  @EnableBinding(Sink.class)
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
      String INPUT = "user-message";
  
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

## 通过命令行查看消息

> sh ./bin/kafka-console-consumer.sh --bootstrap-server 192.168.0.131:9092 --topic test --from-beginningworld

# 注意事项

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

  - 解决报错

    ```yaml
    
    ```

    