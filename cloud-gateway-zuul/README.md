# 网关

> A gateway is a link between two computer programs or systems such as Internet Forums. A gateway acts as a portal between two programs allowing them to share information by communicating using protocols on a computer or between dissimilar computers.

# 项目集成

## 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
```

## 开启反向代理

```java
package org.liangxiong.zuul;

/**
 * @author liangxiong
 * @Date:2019-03-20
 * @Time:13:31
 * @Description 网关服务
 */
@EnableZuulProxy
@SpringBootApplication
public class ZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}
```

## 配置反向代理路径配置规则(application.yml)

```yaml
server:
    port: 8092
management:
    port: 9013
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-zuul-gateway
zuul:
    routes:
        # 代理服务提供方(名称"user-server"可以任意定义)
        user-server:
            path: /feign/**
            serviceId: spring-cloud-user-server
        # 代理客户端(名称"user-clietn"可以任意定义)
        user-client:
            path: /ribbon/**
            serviceId: spring-cloud-ribbon-client
```

## 引入配置客户端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

## 引入Eureka客户端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## 配置中心客户端配置(bootstrap.yml)

```yaml
spring:
    cloud:
        config:
            name: user
            profile: test
            label: master
            discovery:
                enabled: true
                service-id: spring-cloud-config-server-as-client-for-eureka
eureka:
    client:
        service-url:
            defaultZone: http://localhost:8083/eureka/
```

## 通过代理访问

- 服务端访问

> `http://localhost:8092/spring-cloud-user-server/feign/users/`
>
> `http://localhost:8092/spring-cloud-user-server/feign/users/1`

- 代理到客户端访问

> `http://localhost:8092/spring-cloud-ribbon-client/ribbon/remote/users/`
>
> `http://localhost:8092/spring-cloud-ribbon-client/feign/users/`

## 获取代理的路由列表

> `<http://localhost:9013/routes>`

**内容如下所示**

```json
{
    "/feign/**" : "spring-cloud-user-server",
    "/ribbon/**" : "spring-cloud-ribbon-client",
    "/spring-cloud-user-server/**" : "spring-cloud-user-server",
    "/spring-cloud-ribbon-client/**" : "spring-cloud-ribbon-client",
    "/spring-cloud-config-server-as-client-for-eureka/**" : "spring-cloud-config-server-as-client-for-eureka"
}
```

