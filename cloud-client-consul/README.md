# Spring Cloud Eureka高可用

## 客户端高可用

> 如果eureka客户端配置了多个eureka注册中心,默认情况下会连接到第一台可用地注册中心(只有它存在注册信息),如果第一台注册中心down,那么会去连接第二台可用地注册中心.

### 常见配置方式

- 多个注册中心
- 注册中心DNS转发
- 广播

### 配置客户端

- 配置eureka客户端使用多个注册中心

```yaml
server:
    port: 8087
management:
    port: 9008
    security:
        enabled: false
# 配置注册中心地址
eureka:
    client:
        service-url:
            # 多个注册中心使用逗号分隔
            defaultZone: http://localhost:8088/eureka/,http://localhost:18088/eureka/
        # 从注册中心获取配置中心间隔时间
        registry-fetch-interval-seconds: 15
        # 客户端副本改变信息通知eureka服务器
        instance-info-replication-interval-seconds: 15
spring:
    application:
        name: cloud-client-eureka-high-available

```

- 通过命令行参数,模拟多个注册中心启动

> --server.port=18088 --management.port=19009 --management.security.enabled=false

- 访问注册中心

  - 第一台注册中心`http://localhost:8083/`
  - 第二台注册中心`http://localhost:18083/`

- 查看浏览器

  - 第一台注册中心存在客户端实例信息
  - 第二台注册中心没有客户端实例信息

- 关闭第一台注册中心`http://localhost:8083/`

  > 一段时间以后(几秒钟),第二台注册中心获取客户端实例信息

- 再次启动第一台注册中心`http://localhost:8083`

  > 第一台注册中心作为备份,没有客户端实例信息

- 关闭第二台注册中心`http://localhost:18083`

  > 一段时间以后(几秒钟),第一台注册中心获取客户端实例信息

- 通过命令行参数,模拟多个eureka客户端启动

  > --server.port=18087 --management.port=19008 --management.security.enabled=false

- 缺点

  - 多个注册中心中只有一个注册中心存在客户端实例信息

    > 当前注册中心挂掉后,转义注册中心时,需要复制客户端实例信息,如果客户端实例过多,则性能影响严重

  - 当第一台注册中心没有客户端实例信息,第二台注册中心存在客户端实例信息,此时如果客户端重启

    > 客户端会去第一台注册中心注册信息(**而不是直接连接已有客户端信息地第二台注册中心**)

  - 

## 服务端高可用

### 配置多个注册中心

> 注册中心提供集群环境,解决注册中心单点故障问题;注册中心之间也需要进行信息同步处理.

- 配置eureka服务端,新增两个配置文件application-peer1.yml和application-peer2.yml

- 修改application.yml配置文件如下

```yaml
management:
    security:
        enabled: false
spring:
    profiles:
        active: peer1
    application:
        name: spring-cloud-eureka-server
```

- **第一台**服务端实例,application-peer1.yml配置文件如下

```yaml
# 第二个注册中心
server:
    port: 8088
# 与peer2节点同步数据
peer2:
    server:
        host: localhost
        port: 18088
# 配置eureka注册中心数据同步
eureka:
    client:
        # 连接集群中另外地注册中心
        service-url:
            defaultZone: http://${peer2.server.host}:${peer2.server.port}/eureka
management:
    port: 9009
```

- **第二台**服务端实例,application-peer2.yml配置文件如下

```yaml
# 第二个注册中心
server:
    port: 18088
# 与peer1节点同步数据
peer1:
    server:
        host: localhost
        port: 8088
# 配置eureka注册中心数据同步
eureka:
    client:
        # 连接集群中另外地注册中心
        service-url:
            defaultZone: http://${peer1.server.host}:${peer1.server.port}/eureka
management:
    port: 19009
```

### 配置多个客户端实例

- 配置eureka客户端,新增两个配置文件application-instance1.yml和application-instance2.yml
- 修改application.yml文件

```yaml
management:
    security:
        enabled: false
spring:
    application:
        name: cloud-client-eureka-high-available
    profiles:
        active: instance1
```

- **第一台**客户端实例,application-instance1.yml配置文件如下

```yaml
# 第一个客户端实例
server:
    port: 8087
management:
    port: 9008
# 配置注册中心地址
eureka:
    client:
        service-url:
            # 多个注册中心使用逗号分隔
            defaultZone: http://localhost:8088/eureka/,http://localhost:18088/eureka/
        # 从注册中心获取所有客户端配置信息地间隔时间
        registry-fetch-interval-seconds: 5
        # 客户端副本改变信息通知eureka服务器
        instance-info-replication-interval-seconds: 5
spring:
    application:
        name: cloud-client-eureka-high-available
```

- **第二台**客户端实例application-instance2.yml配置文件如下

```yaml
# 第二个客户端实例
server:
    port: 18087
management:
    port: 19008
# 配置注册中心地址
eureka:
    client:
        service-url:
            # 多个注册中心使用逗号分隔
            defaultZone: http://localhost:8088/eureka/,http://localhost:18088/eureka/
        # 从注册中心获取所有客户端配置信息地间隔时间
        registry-fetch-interval-seconds: 5
        # 客户端副本改变信息通知eureka服务器
        instance-info-replication-interval-seconds: 5
spring:
    application:
        name: cloud-client-eureka-high-available
```

### 启动注册中心

- 启动服务端第一个实例

```shell
--spring.profiles.active=peer1
```

- 启动服务端第二个实例

```shell
--spring.profiles.active=peer2
```

### 启动客户端

- 启动客户端第一个实例

```shell
--spring.profiles.active=instance1
```

- 启动客户端第二个实例

```shell
--spring.profiles.active=instance2
```

## Spring Cloud Consul

- 下载

```tex
https://www.consul.io/downloads.html
```

- 安装
- 启动agent

```shell
 .\consul.exe agent -bind="127.0.0.1" --data-dir=F:\temp\consul
```

