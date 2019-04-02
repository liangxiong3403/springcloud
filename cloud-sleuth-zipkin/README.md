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
import zipkin.server.internal.EnableZipkinServer;

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
```

