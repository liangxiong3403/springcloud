

# 服务发现(Service Discovery)

## wikipedia

> Service discovery is the automatic detection of devices and services offered by these devices on a computer network. A service discovery protocol (SDP) is a network protocol that helps accomplish service discovery. Service discovery aims to reduce the configuration efforts from users.

## 中文翻译(非官方,人工翻译)

> 服务发现是计算机网络上的设备和这些设备所提供地服务地自动检测.服务发现协议是帮助实现服务发现地一种网络协议.服务发现目标是减少人工干预配置项.

# 服务注册(Service Registration)

> 在计算机网络中,为了更好地治理多个设备或服务,这些设备或服务主动或被动地注册到管理中心,以便服务被注册和消费.

# Eureka Server

## 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## 激活服务端

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

```

## 配置application.yml

```yaml
server:
    port: 8083
spring:
    application:
        name: spring-cloud-eureka-server
management:
    security:
        enabled: false
    port: 9004
```

## 启动报错

```tex
com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect
....
com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server
```

- 报错原因

> 因为每一个应用既是服务提供者,又是服务消费者;当其作为客户端,无法找到服务提供者时,就会报错.

- 访问`http://localhost:9004/health`

> **description**: "Composite Discovery Client"

# Eureka Client

## 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## 激活客户端

```java
package org.liangxiong.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:10:06
 * @Description eureka客户端;可以使用@EnableEurekaClient或@EnableDiscoveryClient
 */
@EnableEurekaClient
@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}

```

## 配置application.yml

```yaml
server:
    port: 8084
management:
    security:
        enabled: false
    port: 9005
spring:
    application:
        name: spring-cloud-eureka-client
```

## 启动报错(和服务端报错一致)

```tex
com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect
...
com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server
```

# 补充配置

## 解决客户端启动报错

- 修改eureka**客户端**配置application.yml

```yaml
# 配置注册中心地址
eureka:
    client:
        serviceUrl:
            defaultZone: http://127.0.0.1:8083/eureka/
```

## 解决服务端启动报错

- 修改eureka**服务端**配置application.yml

```yaml
# 配置eureka注册中心
eureka:
    client:
        # 注册中心本身不需要借助eureka注册到其他注册中心,只提供服务
        register-with-eureka: false
        # 不需要从eureka获取注册中心信息(服务/实例信息)
        fetch-registry: false
```

## 配置实例状态检查页面相对地址

- 修改eureka**客户端**配置application.yml

```yaml
eureka:
	instance:
        # 配置状态检查页面地址(使用绝对地址,默认是/info)
        status-page-url-path: /status
        # 配置健康检查页面地址(使用相对地址,默认是/health)
        health-check-url-path: /health
```

# Eureka与Spring Cloud Config 整合

## 新建项目cloud-config-server-as-client-for-eureka

> 将Spring Cloud Config Server作为Eureka客户端与Eureka服务端整合

## 引入Maven依赖

- Spring Cloud Config Server相关依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

- Eureka 客户端相关依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## 配置项目启动类

```java
package org.liangxiong.eureka.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:15:40
 * @Description spring config server作为eureka客户端
 */
@EnableDiscoveryClient
@EnableConfigServer
@SpringBootApplication
public class ConfigServerAsEurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerAsEurekaClientApplication.class, args);
    }
}
```

## 配置application.yml

```yaml
server:
    port: 8085
management:
    security:
        enabled: false
    port: 9006
spring:
    application:
        name: spring-cloud-config-server-as-client-for-eureka
    cloud:
        config:
            server:
                git:
                    uri: https://github.com/liangxiong3403/temp
                    force-pull: true
eureka:
    client:
        # 配置eureka注册中心地址
        service-url: 
            defaultZone: http://127.0.0.1:8083/eureka/
```

## 将`spring-cloud-eureka-client`项目作为`cloud-config-server-as-client-for-eureka`项目地配置客户端

- 修改`spring-cloud-eureka-client`配置文件bootstrap.yml

```yaml
spring:
    cloud:
        config:
            # 配置服务器地址
            uri: http://127.0.0.1:8085
            # 获取指定应用名称配置文件
            name: user
            # 配置文件所属环境
            profile: test
            # 配置文件标签
            label: master
```

## 重启`spring-cloud-eureka-client`项目

## 将cloud-config-server-as-client-for-eureka设置为Eureka客户端

- 修改配置文件application.yml,添加如下配置

```yml
spring:
    cloud:
    	config:
            discovery:
                # 配置服务发现
                enabled: true
                service-id: spring-cloud-config-server-as-client-for-eureka
```

