# 搭建Zipkin服务实例

## 新建项目`cloud-sleuth-zipkin`

## 添加依赖

```xml
<dependency>
    <groupId>io.zipkin.java</groupId>
    <artifactId>zipkin-server</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.java</groupId>
    <artifactId>zipkin-autoconfigure-ui</artifactId>
</dependency>
```

## 配置项目

```yml
server:
    port: 8093
management:
    port: 9014
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-zipkin-sleuth
```

## 配置应用程序

```java
package org.liangxiong.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin.server.EnableZipkinServer;

/**
 * @author liangxiong
 * @Date:2019-04-02
 * @Time:16:41
 * @Description Zipkin服务器应用
 */
@EnableZipkinServer
@SpringBootApplication
public class ZipkinServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication.class, args);
    }
}
```

# 配置Zipkin客户端`cloud-client-ribbon`

## 项目`cloud-client-ribbon`添加依赖

```xml
<!-- Sleuth Zipkin相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

## 配置项目

```yml
spring:
    zipkin:
        base-url: http://localhost:8093/
        # HTTP采集数据
        sender:
            type: web
    # 必须配置
    sleuth:
        sampler:
            percentage: 1.0
```

# 配置Zipkin客户端`cloud-server-user`

## 项目`cloud-server-user`添加依赖

```xml
<!-- Sleuth Zipkin相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

## 配置项目

```yml
spring:
    zipkin:
        base-url: http://localhost:8093/
        # HTTP采集数据
        sender:
            type: web
    # 必须配置
    sleuth:
        sampler:
            percentage: 1.0
```



# 项目改造为MQ接收消息

## 改造Zipkin服务器

### 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

### 更改激活服务端方式为stream

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author liangxiong
 * @Date:2019-04-02
 * @Time:16:41
 * @Description Zipkin服务器应用
 */
@EnableZipkinStreamServer
@SpringBootApplication
public class ZipkinServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication.class, args);
    }
}
```

### 修改配置文件

```yaml
server:
    port: 8093
management:
    port: 9014
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-zipkin-sleuth
    cloud:
        stream:
            rabbit:
                binder:
                    adminAddresses: 192.168.0.130,192.168.0.131,192.168.0.132
                    nodes: 192.168.0.130,192.168.0.131,192.168.0.132
    rabbitmq:
        addresses: 192.168.0.130,192.168.0.131,192.168.0.132
        username: admin
        password: 123456
```

## 改造`cloud-client-ribbon`

> 应用`trace`信息放在了MQ,保证了持久化和吞吐量

### 删除依赖(它会和stream相关依赖冲突)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

### 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-stream</artifactId>
</dependency>
```

### 删除配置

```yaml
#    zipkin:
#        base-url: http://localhost:8093/
```

### 添加配置

```yaml
spring:
    application:
        name: spring-cloud-ribbon-client
    cloud:
        stream:
        	default-binder: rabbit
            rabbit:
                binder:
                    adminAddresses: 192.168.0.130,192.168.0.131,192.168.0.132
                    nodes: 192.168.0.130,192.168.0.131,192.168.0.132
    rabbitmq:
        addresses: 192.168.0.130,192.168.0.131,192.168.0.132
        username: admin
        password: 123456
    zipkin:
        # Rabbit采集数据
        sender:
            type: rabbit
    # Otherwise you might think that Sleuth is not working cause it’s omitting some spans.
    sleuth:
        sampler:
            percentage: 1.0
```

## 改造`cloud-server-user`

### 删除依赖(它会和stream相关依赖冲突)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

### 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

### 删除配置

```yaml
#    zipkin:
#        base-url: http://localhost:8093/
```

### 添加配置

```yaml
spring:
    application:
        name: spring-cloud-user-server
    cloud:
        stream:
        	default-binder: rabbit
            rabbit:
                binder:
                    adminAddresses: 192.168.0.130,192.168.0.131,192.168.0.132
                    nodes: 192.168.0.130,192.168.0.131,192.168.0.132
    rabbitmq:
        addresses: 192.168.0.130,192.168.0.131,192.168.0.132
        username: admin
        password: 123456
    zipkin:
        # Rabbit采集数据
        sender:
            type: rabbit
    # Otherwise you might think that Sleuth is not working cause it’s omitting some spans.
    sleuth:
        sampler:
            percentage: 1.0
```

# 注意事项

## 当使用HTTP方式传递信息给Zipkin时,会中断断路器设置中的sleep线程

- 配置文件

```yaml
spring:
    zipkin
        # HTTP采集数据
        sender:
            type: web
```

- java代码

```java
package org.liangxiong.server.provider.controller;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:16:04
 * @Description 用户服务提供方(Feign方式)
 */
@Slf4j
@RestController
public class UserProviderFeignController implements IUserService {

    private static final Random RANDOM = new Random();

    @Autowired
    @Qualifier("inMemoryUserServiceImpl")
    private IUserService userService;

    /**
     * 通过id获取指定用户
     *
     * @param userId
     * @return
     */
    @HystrixCommand(commandProperties = @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "300"))
    @Override
    public User getUserById(@PathVariable("userId") Integer userId) {
        try {
            int time = RANDOM.nextInt(500);
            log.info("sleep time: {}", time);
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            log.error("method execution interrupt!");
        }
        return userService.getUserById(userId);
    }

}
```

