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
      <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
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
          listOfServers: rule
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

# Ribbon源码分析

> Ribbon客户端和User服务端直连测试分析源代码,没有通过eureka注册中心

## org.springframework.cloud.client.loadbalancer.LoadBalancerClient

### 主要功能

- 将服务实例名称`spring-cloud-user-server`转化为IP:PORT形式
- 通过负载均衡算法,选择一台服务器
- 确定服务器后,进行回调操作

### 默认实现

- org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration#loadBalancerClient

## com.netflix.loadbalancer.LoadBalancerContext

### 主要功能

- 将服务实例名称`spring-cloud-user-server`转化为IP:PORT形式
- 关联RetryHandler和ILoadBalancer
- 记录服务统计信息,记录请求响应时间,记录请求错误数量

### 默认实现

- org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerContext

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration#ribbonLoadBalancerContext

## com.netflix.loadbalancer.ILoadBalancer(负载均衡器抽象)

### 主要功能

- 添加服务器
- 选择服务器/获取所有服务器等
- 标记服务器下线

### 默认实现

- com.netflix.loadbalancer.ZoneAwareLoadBalancer

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration#ribbonLoadBalancer

## com.netflix.loadbalancer.IRule

### 主要功能

- 获取可用服务器:根据负载均衡算法和服务器key获取可用服务器

### 默认实现

- com.netflix.loadbalancer.ZoneAvoidanceRule

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration#ribbonRule

##　com.netflix.loadbalancer.IPing

### 主要功能

- 根据指定服务器,判断其是否存活

### 默认实现

- com.netflix.loadbalancer.DummyPing

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration#ribbonPing

## com.netflix.loadbalancer.ServerList

### 主要功能

- 获取初始化服务列表
- 获取更新后服务列表

### 默认实现

- com.netflix.loadbalancer.ConfigurationBasedServerList(**Ribbon原生模式**)

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
  spring-cloud-user-server:
      ribbon:
          listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port}
  # 配置eureka服务端信息
  eureka:
      client:
  #        service-url:
  #            defaultZone: http://localhost:8083/eureka/
  #        registry-fetch-interval-seconds: 15
          enabled: false
  ```

- com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList(**Eureka客户端模式**)

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
  #spring-cloud-user-server:
  #    ribbon:
  rule
  # 配置eureka服务端信息
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
  ```

### 自动装配源

- org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration#ribbonServerList

## 自动装配类

### org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration

- LoadBalancerClient
- PropertiesFactory

### org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration

- RestTemplate
- @LoadBalanced

# 调用链路分析

## 自定义代码

```java
@GetMapping("/remote/users")
public List<User> listAllUsers() {
    // 根据服务实例名称获取实例对象(此刻端点调试)
    ServiceInstance serviceInstance = loadBalancerClient.choose(remoteServiceProviderApplicationName);
    try {
        return loadBalancerClient.execute(remoteServiceProviderApplicationName, serviceInstance, request -> {
            RestTemplate restTemplate = new RestTemplate();
            // 通过serviceInstance获取本地请求主机地址
            String host = request.getHost();
            // 通过serviceInstance获取本地请求主机端口
            int port = request.getPort();
            StringBuffer url = new StringBuffer();
            // 构造真实URL
            url.append("http://").append(host).append(":").append(port).append("/users");
            return restTemplate.getForObject(url.toString(), List.class);
        });
    } catch (IOException e) {
        log.error("remote execute error: {}", e.getMessage());
    }
    return Collections.emptyList();
}
```

> 代码调试
>
> ServiceInstance serviceInstance = loadBalancerClient.choose(remoteServiceProviderApplicationName);

- 第一步

> org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient#choose

- 第二步

> com.netflix.loadbalancer.ZoneAwareLoadBalancer#chooseServer

- 第三步

> com.netflix.loadbalancer.BaseLoadBalancer#chooseServer

- 第四步

> com.netflix.loadbalancer.PredicateBasedRule#choose

# 自定义实现Rule

## 自定义bean实现IRule

```JAVA
/**
 * @author liangxiong
 * @Date:2019-03-11
 * @Time:14:21
 * @Description 自定义实现IRule规则
 */
@Component
public class DiyRuleImpl extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }

    @Override
    public Server choose(Object key) {
        Random random = new Random();
        // 获取负载均衡器
        ILoadBalancer loadBalancer = getLoadBalancer();
        // 获取可用服务列表
        List<Server> servers = loadBalancer.getReachableServers();
        if (!CollectionUtils.isEmpty(servers)) {
            // 随机获取一台可用服务
            return servers.get(random.nextInt(servers.size()));
        }
        return null;
    }
}
```

## `cloud-client-ribbon`配置文件application.yml

> 原生ribbon方式,直接通过服务名称连接到服务provider

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
        provider2:
            application:
                name: spring-cloud-user-server
            host: localhost
            port: 18090
# 配置ribbon服务提供方(缺点是需要手动配置,生产环境应使用eureka注册中心来发现服务端)
spring-cloud-user-server:
    ribbon:
        listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port},${remote.service.provider2.host}:${remote.service.provider2.port}
# 配置eureka服务端信息
eureka:
#    client:
#        service-url:
#            defaultZone: http://localhost:8083/eureka/
#        registry-fetch-interval-seconds: 15
		# 引入eureka客户端依赖后,临时关闭eureka客户端功能
        enabled: false
```

## `cloud-server-user`配置文件application.yml

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
#    client:
#        service-url:
#            defaultZone: http://localhost:8083/eureka/
#        registry-fetch-interval-seconds: 15
		# 引入eureka客户端依赖后,临时关闭eureka客户端功能
        enabled: false
```

# 自定义实现IPing

> 参考Spring Cloud官方文档:16.4 Customizing the Ribbon Client using properties

## 自定义bean实现IPing

```java
/**
 * @author liangxiong
 * @Date:2019-03-11
 * @Time:15:00
 * @Description 自定义实现IPing规则
 */
public class DiyPingImpl implements IPing {

    @Override
    public boolean isAlive(Server server) {
        String host = server.getHost();
        int port = server.getPort();
        // Spring 工具类构建
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("http");
        builder.host(host);
        builder.port(port);
        builder.path("/users");
        URI uri = builder.build().toUri();
        // 发送请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity responseEntity = restTemplate.getForEntity(uri, List.class);
        // 判断响应状态码
        return HttpStatus.OK.equals(responseEntity.getStatusCode());
    }
}
```

## 修改`cloud-client-ribbon`配置文件application.yml

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
        provider2:
            application:
                name: spring-cloud-user-server
            host: localhost
            port: 18090
# 配置ribbon服务提供方(缺点是需要手动配置,生产环境应使用eureka注册中心来发现服务端)
spring-cloud-user-server:
    ribbon:
        listOfServers: http://${remote.service.provider.host}:${remote.service.provider.port},${remote.service.provider2.host}:${remote.service.provider2.port}
        # 通过配置方式引入自定义IPing实现类
        NFLoadBalancerPingClassName: org.liangxiong.ribbon.ping.DiyPingImpl
# 配置eureka服务端信息
eureka:
#    client:
#        service-url:
#            defaultZone: http://localhost:8083/eureka/
#        registry-fetch-interval-seconds: 15
#        # 引入eureka客户端依赖后,临时关闭eureka客户端功能
        enabled: false
```

# 集成Feign

## 项目`cloud-api-user`添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## 项目`cloud-api-user`声明Feign客户端接口

```java
package org.liangxiong.cloud.api.service;

/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:9:57
 * @Description 用户操作业务层
 */
@FeignClient("${provider.user.service.name}")
public interface IUserService {

    /**
     * 添加用户,feign指定请求路径
     *
     * @param user
     * @return
     */
    @PostMapping("/users")
    boolean addUser(User user);

    /**
     * 获取所有地用户,feign指定请求路径
     *
     * @return
     */
    @GetMapping("/users")
    List<User> listAllUsers();

    /**
     * 通过id获取指定用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/users/{userId}")
    User getUserById(Integer userId);
}
```

## 项目`cloud-client-ribbon`激活Feign客户端组件扫描

```java
/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:9:50
 * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端,Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
 */
@EnableFeignClients(clients = IUserService.class)
@EnableCircuitBreaker
@EnableDiscoveryClient
@RibbonClients(@RibbonClient(name = "spring-cloud-ribbon-client"))
@SpringBootApplication
public class RibbonClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RibbonClientApplication.class, args);
    }
}
```

## 客户端`cloud-client-ribbon`指定占位符名称

```yaml
# 指定cloud-api-user项目中@FeignClient("${provider.user.service.name}")
provider:
    user:
        service:
            name: ${remote.service.provider.application.name}
```

## 服务端`cloud-server-user`提供服务

```java
/**
 * @author liangxiong
 * @Date:2019-03-10
 * @Time:10:02
 * @Description 内存中地实现, 没有访问持久层;集成Feign时,指定bean名称
 */
@Service("inMemoryUserServiceImpl")
public class InMemoryUserServiceImpl implements IUserService {

    private Map<Integer, User> repository = new ConcurrentHashMap<>(8);

    @Override
    public boolean addUser(User user) {
        return repository.put(user.getUserId(), user) == null;
    }

    @Override
    public List<User> listAllUsers() {
        return new ArrayList<>(repository.values());
    }
}
```

## 服务端`cloud-server-user`控制器

- 服务端控制器

```java
package org.liangxiong.server.provider.controller;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:16:04
 * @Description 用户服务提供方(Feign方式)
 */
@RequestMapping("/diy/feign/server")
@RestController
public class UserProviderFeignController implements IUserService {

    @Autowired
    @Qualifier("inMemoryUserServiceImpl")
    private IUserService userService;

    /**
     * @param user 输入参数;path对应服务端POST:/users
     * @return
     */
    @Override
    public boolean addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    /**
     * path对应服务端GET:/users
     *
     * @return 所有用户列表
     */
    @Override
    public List<User> listAllUsers() {
        return userService.listAllUsers();
    }
}
```

- 访问`localhost:8090/feign/users`测试本地接口是否正常

  > 路径组成: Controller中路径+  @Override方法接口中包含的路径`/feign/users`

## 客户端`cloud-client-ribbon`调用远程服务

- 客户端控制器

```java
package org.liangxiong.ribbon.controller.feign;

/**
 * @author liangxiong
 * @Date:2019-03-18
 * @Time:15:48
 * @Description Feign的方式调用远程服务;注意:官方不推荐客户端和服务端同时实现feign客户端接口(比如IUserService)
 */
@RestController
public class FeignClientController implements IUserService {

    /**
     * 未报错是因为@FeignClient mark the feign proxy as a primary bean
     */
    @Autowired
    private IUserService userService;

    /**
     * @param user 输入参数;path对应服务端POST:/users
     * @return
     */
    @Override
    public boolean addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    /**
     * path对应服务端GET:/users
     *
     * @return 所有用户列表
     */
    @Override
    public List<User> listAllUsers() {
        return userService.listAllUsers();
    }

    /**
     * 通过id获取指定用户
     *
     * @param userId
     * @return
     */
    @Override
    public User getUserById(@PathVariable("userId") Integer userId) {
        return userService.getUserById(userId);
    }
}
```

- 访问`http://localhost:8089/feign/users`测试远程接口是否正常

  > 路径组成: Controller中路径+  @Override方法接口中包含的路径`/feign/users

- 客户端请求报错

  ```tex
  "timestamp":1552914058014,"status":405,"error":"Method Not Allowed","exception":"org.springframework.web.HttpRequestMethodNotSupportedException","message":"Request method 'POST' not supported","path":"/feign/users/id"}
  ```

- 解决客户端报错(修改接口参数声明方式)

  ```java
  /**
   * 通过id获取指定用户(@RequestParam解决客户端请求报错)
   *
   * @param userId
   * @return
   */
  @GetMapping("/feign/users/{userId}")
  User getUserById(@PathVariable("userId") Integer userId);
  ```

## 项目集成hystrix

- 创建回调类

  ```java
  package org.liangxiong.cloud.api.fallback;
  
  /**
   * @author liangxiong
   * @Date:2019-03-18
   * @Time:21:51
   * @Description 回调实现类, 用于feign的断路器配置
   */
  public class UserServiceFallback implements IUserService {
  
      @Override
      public boolean addUser(User user) {
          return false;
      }
  
      @Override
      public List<User> listAllUsers() {
          return Collections.EMPTY_LIST;
      }
  
      @Override
      public User getUserById(Integer userId) {
          return null;
      }
  }
  ```

- 配置回调类

  ```java
  package org.liangxiong.cloud.api.service;
  
  /**
   * @author liangxiong
   * @Date:2019-03-10
   * @Time:9:57
   * @Description 用户操作业务层
   */
  @FeignClient(value = "${provider.user.service.name}", fallback = UserServiceFallback.class)
  public interface IUserService {
  
      /**
       * 添加用户,feign指定请求路径
       *
       * @param user
       * @return
       */
      @PostMapping("/feign/users")
      boolean addUser(User user);
  
      /**
       * 获取所有地用户
       *
       * @return
       */
      @GetMapping("/feign/users")
      List<User> listAllUsers();
  
      /**
       * 通过id获取指定用户(@RequestParam解决客户端请求报错)
       *
       * @param userId 用户id
       * @return
       */
      @GetMapping("/feign/users/{userId}")
      User getUserById(@PathVariable("userId") Integer userId);
  }
  ```

- 配置服务端服务熔断条件

  ```java
  package org.liangxiong.server.provider.controller;
  
  /**
   * @author liangxiong
   * @Date:2019-03-18
   * @Time:16:04
   * @Description 用户服务提供方(Feign方式)
   */
  @RestController
  public class UserProviderFeignController implements IUserService {
  
      @Autowired
      @Qualifier("inMemoryUserServiceImpl")
      private IUserService userService;
  
      /**
       * @param user 输入参数;path对应服务端POST:/users
       * @return
       */
      @Override
      public boolean addUser(@RequestBody User user) {
          return userService.addUser(user);
      }
  
      /**
       * path对应服务端GET:/users
       *
       * @return 所有用户列表
       */
      @Override
      public List<User> listAllUsers() {
          return userService.listAllUsers();
      }
  
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

- 配置客户端`cloud-client-ribbon`,支持hystrix

  ```yaml
  # 开启feign对hystrix的支持
  feign:
      hystrix:
          enabled: true
  ```

- 客户端`cloud-client-ribbon`报错

  ```tex
  Caused by: java.lang.IllegalStateException: No fallback instance of type class org.liangxiong.cloud.api.fallback.UserServiceFallback found for feign client spring-cloud-user-server
  ```

- 解决客户端`cloud-client-ribbon`报错

  ```java
  package org.liangxiong.ribbon.config;
  
  /**
   * @author liangxiong
   * @Date:2019-03-09
   * @Time:13:55
   * @Description 配置类
   */
  @Configuration
  public class WebConfiguration {
  
      /**
       * 实现RestTemplate实例的负载均衡
       *
       * @return
       */
      @Bean
      @LoadBalanced
      public RestTemplate restTemplate() {
          return new RestTemplate();
      }
  
      /**
       * 解决feign中开启hystrix的报错
       *
       * @return
       */
      @Bean
      public UserServiceFallback userServiceFallback() {
          return new UserServiceFallback();
      }
  }
  ```

- 访问客户端地址(客户端内部远程调用),测试断路器是否正常

  > `http://localhost:8089/feign/users/2`

  - 正常情况

    ```json
    {
        userId: 2,
        username: "薛宝钗",
        age: 16
    }
    ```

  - 异常情况

    ```json
    {
        userId: 999,
        username: "undefined",
        age: 0
    }
    ```

    