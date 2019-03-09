# 客户端负载均衡

## 特点

### 缺点

- 负载均衡功能和客户端绑定
- 升级比较麻烦

### 优点

- 单个客户端负载均衡错误,影响面积较小

## 配置客户端

- 创建项目`cloud-client-ribbon`

- 引入依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-ribbon</artifactId>
  </dependency>
  ```

- 激活ribbon客户端

  ```java
  package org.liangxiong.ribbon;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.netflix.ribbon.RibbonClient;
  import org.springframework.cloud.netflix.ribbon.RibbonClients;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:9:50
   * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端
   */
  @RibbonClients(@RibbonClient(name = "spring-cloud-ribbon-client"))
  @SpringBootApplication
  public class RibbonClientApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(RibbonClientApplication.class, args);
      }
  }
  ```

- 配置application.yml

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
  # 服务提供方配置信息
  remote:
      service:
          provider:
              application:
                  name: spring-cloud-user-server
              host: localhost
              port: 8090
  # 配置ribbon服务提供方(缺点是需要手动配置,生产环境应使用eureka注册中心来发现服务端)
  spring-cloud-user-service:
      ribbon:
          listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port}
  ```

- 配置RestTemplate

  ```java
  package org.liangxiong.ribbon.config;
  
  import org.springframework.cloud.client.loadbalancer.LoadBalanced;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.web.client.RestTemplate;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:13:55
   * @Description 配置类
   */
  @Configuration
  public class WebConfiguration {
  
      @Bean
      @LoadBalanced
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
  }
  ```

- 服务远程调用

  ```java
  package org.liangxiong.ribbon.controller;
  
  import com.alibaba.fastjson.JSONObject;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.web.bind.annotation.PostMapping;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;
  import org.springframework.web.client.RestTemplate;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:11:30
   * @Description ribbon作为客户端Controller
   */
  @RequestMapping("/ribbon")
  @RestController
  public class RibbonClientController {
  
      /**
       * 远程服务提供方主机
       */
      @Value("${remote.service.provider.host}")
      private String remoteServiceProviderHost;
  
      /**
       * 远程服务提供方端口
       */
      @Value("${remote.service.provider.port}")
      private String remoteServiceProviderPort;
  
      /**
       * 远程服务提供方名称
       */
      @Value("${remote.service.provider.application.name}")
      private String remoteServiceProviderApplicationName;
  
      @Autowired
      private RestTemplate restTemplate;
  
      @PostMapping("/remote/user")
      public Object getRemoteUser(@RequestBody JSONObject params) {
          StringBuffer url = new StringBuffer();
          // 方式一
          ///url.append("http://").append(remoteServiceProviderHost).append(":").append(remoteServiceProviderPort).append("/users");
          // 方式二
          url.append("http://").append(remoteServiceProviderApplicationName).append("/users");
          return restTemplate.postForObject(url.toString(), params, Object.class);
      }
  
  }
  ```

- ribbon客户端日志信息(当访问接口时产生)

  ```tex
  2019-03-09 15:13:25.114 | INFO  | http-nio-8089-exec-2 | com.netflix.config.ChainedDynamicProperty | Flipping property: spring-cloud-user-server.ribbon.ActiveConnectionsLimit to use NEXT property: niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit = 2147483647
  2019-03-09 15:13:25.114 | INFO  | http-nio-8089-exec-2 | com.netflix.loadbalancer.DynamicServerListLoadBalancer | DynamicServerListLoadBalancer for client spring-cloud-user-server initialized: DynamicServerListLoadBalancer:{NFLoadBalancer:name=spring-cloud-user-server,current list of Servers=[localhost:8090],Load balancer stats=Zone stats: {unknown=[Zone:unknown;	Instance count:1;	Active connections count: 0;	Circuit breaker tripped count: 0;	Active connections per server: 0.0;]
  },Server stats: [[Server:localhost:8090;	Zone:UNKNOWN;	Total Requests:0;	Successive connection failure:0;	Total blackout seconds:0;	Last connection made:Thu Jan 01 08:00:00 CST 1970;	First connection made: Thu Jan 01 08:00:00 CST 1970;	Active Connections:0;	total failure count in last (1000) msecs:0;	average resp time:0.0;	90 percentile resp time:0.0;	95 percentile resp time:0.0;	min resp time:0.0;	max resp time:0.0;	stddev resp time:0.0]
  ]}ServerList:com.netflix.loadbalancer.ConfigurationBasedServerList@1aac2215
  2019-03-09 15:13:26.132 | INFO  | PollingServerListUpdater-0 | com.netflix.config.ChainedDynamicProperty | Flipping property: spring-cloud-user-server.ribbon.ActiveConnectionsLimit to use NEXT property: niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit = 2147483647
  ```

- 调用链路解析(服务名称转化为ip:host)

  - org.springframework.cloud.client.loadbalancer.LoadBalancerClient#execute(java.lang.String, org.springframework.cloud.client.ServiceInstance, org.springframework.cloud.client.loadbalancer.LoadBalancerRequest<T>)
    - org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient#execute(java.lang.String, org.springframework.cloud.client.ServiceInstance, org.springframework.cloud.client.loadbalancer.LoadBalancerRequest<T>)

## 用户服务端修改为Eureka客户端

> 项目`cloud-server-user`作为`cloud-server-eureka`的客户端,保证ribbon客户端只和eureka注册中心交互

- 项目`cloud-server-user`引入eureka客户端相关依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

- 修改配置文件application.yml

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
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
  ```

- 激活客户端配置

  ```java
  package org.liangxiong.server;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:11:04
   * @Description 用户服务提供者, Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
   */
  @EnableDiscoveryClient
  @SpringBootApplication
  public class UserProviderApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(UserProviderApplication.class, args);
      }
  }
  ```

## Ribbon客户端配置为Eureka客户端

- 引入依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```

- 修改配置文件application.yaml

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
  # 服务提供方配置信息
  remote:
      service:
          provider:
              application:
                  name: spring-cloud-user-server
              host: localhost
              port: 8090
  # 配置ribbon服务提供方
  spring-cloud-user-server:
      ribbon:
          listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port}
  # 配置eureka服务端信息
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
  ```

- 激活eureka客户端配置

  ```java
  package org.liangxiong.ribbon;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
  import org.springframework.cloud.netflix.ribbon.RibbonClient;
  import org.springframework.cloud.netflix.ribbon.RibbonClients;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:9:50
   * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端,Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
   */
  @EnableDiscoveryClient
  @RibbonClients(@RibbonClient(name = "spring-cloud-ribbon-client"))
  @SpringBootApplication
  public class RibbonClientApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(RibbonClientApplication.class, args);
      }
  }
  ```

- 当使用Eureka注册中心时,修改ribbon客户端`cloud-client-ribbon`地配置

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
  # 服务提供方配置信息
  remote:
      service:
          provider:
              application:
                  name: spring-cloud-user-server
              host: localhost
              port: 8090
  # 配置ribbon服务提供方(使用eureka时,注册这部分信息)
  #spring-cloud-user-server:
  #    ribbon:
  #        listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port}
  # 配置eureka服务端信息
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
  ```

## 启动`cloud-server-eureka`(注册中心)

## 启动`cloud-server-user`(服务提供者)

## 启动`cloud-client-ribbon`(ribbon客户端负载均衡)

## 通过修改端口,模拟两个`cloud-server-user`

> --server.port=18090 --management.port=19011
>
> --server.port=28090 --management.port=29011

## 访问`http://localhost:8089/ribbon/remote/user`

> 发现ribbon默认调度算法为随机调度`cloud-server-user`

## TODO

> 当某个服务提供者下线后,eureka注册中心未及时将其剔除

# 服务端负载均衡

## 特点

### 优点

- 负载均衡独立于客户端和服务端

### 缺点

- 单点故障