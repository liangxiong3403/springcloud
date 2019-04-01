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
          listOfServers: http://localhost:8090/
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

# Feign集成注册中心/配置中心/断路器

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

## 集成Eureka(使用注册中心提供服务注册和服务发现,取代Ribbon直接连接服务提供方的方式)

- 服务提供方`cloud-server-user`集成Eureka

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

- 客户端`cloud-server-user`集成Eureka

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
  # 控制@EnableCircuitBreaker方式二
  hystrix:
      stream:
          endpoint:
              enabled: true
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
          # 通过配置方式引入自定义IPing实现类
          NFLoadBalancerPingClassName: org.liangxiong.ribbon.ping.DiyPingImpl
  # 配置eureka服务端信息
  eureka:
      client:
          service-url:
              defaultZone: http://localhost:8083/eureka/
          registry-fetch-interval-seconds: 15
          # 引入eureka客户端依赖后,临时关闭eureka客户端功能
          #enabled: false
  # 指定cloud-api-user项目中@FeignClient("${provider.user.service.name}")
  provider:
      user:
          service:
              name: ${remote.service.provider.application.name}
  # 开启feign对hystrix的支持
  feign:
      hystrix:
          enabled: true
  ```

## 集成Config Server

- 客户端`cloud-client-ribbon`引入依赖

  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-config</artifactId>
  </dependency>
  ```

- 客户端`cloud-client-ribbon`配置**配置中心**地址(bootstrap.yaml)

  ```yaml
  spring:
      cloud:
          config:
              name: user
              profile: test
              label: master
              # 开启对于配置中心的服务发现(通过Eureka找到配置中心服务器)
              discovery:
                  enabled: true
                  service-id: spring-cloud-config-server-as-client-for-eureka
  eureka:
      client:
          # 注册中心地址
          service-url:
              defaultZone: http://localhost:8083/eureka
  ```

- 客户端`cloud-client-ribbon`的配置文件application.yml使用配置文件的配置

  ```yaml
  #当集成配置中心时,"remote.service.provider.application.name"这个配置可以从配置中心获取
  provider:
      user:
          service:
              name: ${remote.service.provider.application.name}
  ```

# 配置客户端`cloud-client-ribbon`超时时间动态配置

## 修改配置文件application.yml

```yaml
# 定义接口超时时间,动态调整
method:
    execution:
        timeout: 30
```

## 修改实现类

```java
package org.liangxiong.ribbon.component;

/**
 * @author liangxiong
 * @Date:2019-03-12
 * @Time:15:22
 * @Description
 */
@Slf4j
public class DiyHystrixCommand extends HystrixCommand<Object> {

    /**
     * 远程服务提供方名称
     */
    private final String remoteServiceProviderApplicationName;

    private final RestTemplate restTemplate;

    /**
     * 前端参数
     */
    private JSONObject params;

    public DiyHystrixCommand(String groupKey, String commandKey, String remoteServiceProviderApplicationName, RestTemplate restTemplate, Integer timeout) {
       // 保证每次请求的commandKey不同,就可以动态调整timeout参数值
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey)).andCommandKey(HystrixCommandKey.Factory.asKey(commandKey + timeout)).andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout)));
        this.remoteServiceProviderApplicationName = remoteServiceProviderApplicationName;
        this.restTemplate = restTemplate;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    /**
     * 替代@EnableHystrix的注解实现
     *
     * @return
     * @throws Exception
     */
    @Override
    public Object run() throws Exception {
        long startTime = System.currentTimeMillis();
        StringBuffer url = new StringBuffer();
        url.append("http://").append(remoteServiceProviderApplicationName).append("/users");
        Map<String, Object> result = restTemplate.postForObject(url.toString(), params, Map.class);
        long endTime = System.currentTimeMillis();
        log.info("time interval: {}", endTime - startTime);
        return result;
    }

    /**
     * 回调方法用于熔断恢复
     */
    @Override
    public Object getFallback() {
        log.error("client execution timeout!");
        return Collections.emptyMap();
    }
}
```

## 客户端控制器配置(需要使用@RefreshScope注解)

```java
package org.liangxiong.ribbon.controller;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:30
 * @Description ribbon作为客户端Controller
 */
@Slf4j
@RefreshScope
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

    /**
     * 获取配置文件超时时间(通过POST http://localhost:9010/env?method.execution.timeout=40设置,同时调用refresh)
     */
    @Value("${method.execution.timeout}")
    private Integer timeout;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    private DiyHystrixCommand diyHystrixCommand;

    /**
     * 添加用户
     *
     * @param params
     * @return
     */
    @PostMapping("/remote/users")
    public Object addRemoteUser(@RequestBody JSONObject params) {
        // 方式三,通过hystrix调用
        try {
            // 解决调用报错
            this.diyHystrixCommand = new DiyHystrixCommand("spring-cloud-ribbon-client-group-key", "spring-cloud-ribbon-client-command-key", remoteServiceProviderApplicationName, restTemplate, timeout);
            // 设置参数
            diyHystrixCommand.setParams(params);
            // 自定义hystrix的command实现
            return diyHystrixCommand.execute();
        } catch (Exception e) {
            log.error("remote call failure: {}", e.getMessage());
        }
        return null;
    }

}
```

## 测试步骤

- 默认超时时间为配置文件设置的30毫秒
- 通过POST方法`http://localhost:9010/env?method.execution.timeout=40`修改超时时间
- 调用POST方法`http://localhost:9010/refresh`刷新应用上下文配置(保证控制器的timeout参数改变)
- 调用POST方法`http://localhost:8089/ribbon/remote/users`提交JSON数据,测试超时时间阈值是否为40毫秒(查看系统日志)

## 源码分析为什么由`commandKey`决定超时参数

- 第一段代码`com.netflix.hystrix.AbstractCommand#AbstractCommand`

```java
this.commandGroup = initGroupKey(group);
this.commandKey = initCommandKey(key, getClass());
this.properties = initCommandProperties(this.commandKey, propertiesStrategy, commandPropertiesDefaults);
this.threadPoolKey = initThreadPoolKey(threadPoolKey, this.commandGroup, this.properties.executionIsolationThreadPoolKeyOverride().get());
this.metrics = initMetrics(metrics, this.commandGroup, this.threadPoolKey, this.commandKey, this.properties);
this.circuitBreaker = initCircuitBreaker(this.properties.circuitBreakerEnabled().get(), circuitBreaker, this.commandGroup, this.commandKey, this.properties, this.metrics);
this.threadPool = initThreadPool(threadPool, this.threadPoolKey, threadPoolPropertiesDefaults);

//Strategies from plugins
this.eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
this.concurrencyStrategy = HystrixPlugins.getInstance().getConcurrencyStrategy();
HystrixMetricsPublisherFactory.createOrRetrievePublisherForCommand(this.commandKey, this.commandGroup, this.metrics, this.circuitBreaker, this.properties);
this.executionHook = initExecutionHook(executionHook);

this.requestCache = HystrixRequestCache.getInstance(this.commandKey, this.concurrencyStrategy);
this.currentRequestLog = initRequestLog(this.properties.requestLogEnabled().get(), this.concurrencyStrategy);

/* fallback semaphore override if applicable */
this.fallbackSemaphoreOverride = fallbackSemaphore;

/* execution semaphore override if applicable */
this.executionSemaphoreOverride = executionSemaphore;
```

- 第二段代码`com.netflix.hystrix.AbstractCommand#initCommandKey`

```java
private static HystrixCommandKey initCommandKey(final HystrixCommandKey fromConstructor, Class<?> clazz) {
    if (fromConstructor == null || fromConstructor.name().trim().equals("")) {
        // 根据AbstractCommand的实现类org.liangxiong.ribbon.component.DiyHystrixCommand获取名称,先从缓存获取,获取不到就根据实现类的类目构造名称,然后放入缓存
        final String keyName = getDefaultNameFromClass(clazz);
        return HystrixCommandKey.Factory.asKey(keyName);
    } else {
        return fromConstructor;
    }
}
```

- 第三段代码`com.netflix.hystrix.AbstractCommand#initCommandProperties`

```java
private static HystrixCommandProperties initCommandProperties(HystrixCommandKey commandKey, HystrixPropertiesStrategy propertiesStrategy, HystrixCommandProperties.Setter commandPropertiesDefaults) {
    // 如果属性策略为空
    if (propertiesStrategy == null) {
        // 根据commandKey获取配置参数(比如execution.isolation.thread.timeoutInMilliseconds)
        return HystrixPropertiesFactory.getCommandProperties(commandKey, commandPropertiesDefaults);
    } else {
        // used for unit testing
        return propertiesStrategy.getCommandProperties(commandKey, commandPropertiesDefaults);
    }
}
```

- 第四段代码`com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory#getCommandProperties`

```java
public static HystrixCommandProperties getCommandProperties(HystrixCommandKey key, HystrixCommandProperties.Setter builder) {
    HystrixPropertiesStrategy hystrixPropertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();
    // cacheKey就是commandKey
    String cacheKey = hystrixPropertiesStrategy.getCommandPropertiesCacheKey(key, builder);
    if (cacheKey != null) {
        HystrixCommandProperties properties = commandProperties.get(cacheKey);
        if (properties != null) {
            return properties;
        } else {
            if (builder == null) {
                builder = HystrixCommandProperties.Setter();
            }
            // create new instance
            properties = hystrixPropertiesStrategy.getCommandProperties(key, builder);
            // cache and return
            HystrixCommandProperties existing = commandProperties.putIfAbsent(cacheKey, properties);
            if (existing == null) {
                return properties;
            } else {
                return existing;
            }
        }
    } else {
        // no cacheKey so we generate it with caching
        return hystrixPropertiesStrategy.getCommandProperties(key, builder);
    }
}
```

- `commandKey`是获取HystrixCommandProperties的关键

# Spring Cloud Bus

## 环境准备(重要)

```tex
需要提前启动RabbitMQ服务器或者Kafka服务器,因为BUS的实现既可以是RabbitMQ,也可以是Kafka
```

## 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

## 访问bus端点(destination=`ApplicationContext` ID)

> `ApplicationContext` ID combination of the `spring.application.name`, active profiles `name` and `server.port` by default

- 单点传播

  > curl -X POST http://192.168.0.97:9010/bus/refresh?destination=spring-cloud-user-server:prod:8090

- 集群传播

  > curl -X POST http://192.168.0.97:9010/bus/refresh?destination=spring-cloud-user-server:**

## 通过日志查看监听器

```tex
2019-04-01 10:42:50.311 | INFO  | http-nio-9010-exec-10 | org.springframework.cloud.bus.event.RefreshListener | Received remote refresh request. Keys refreshed []
```

## 查看监听器

```java
public class RefreshListener
		implements ApplicationListener<RefreshRemoteApplicationEvent> {

	private static Log log = LogFactory.getLog(RefreshListener.class);

	private ContextRefresher contextRefresher;

	public RefreshListener(ContextRefresher contextRefresher) {
		this.contextRefresher = contextRefresher;
	}

	@Override
	public void onApplicationEvent(RefreshRemoteApplicationEvent event) {
		Set<String> keys = contextRefresher.refresh();
		log.info("Received remote refresh request. Keys refreshed " + keys);
	}
}
```

## 查看事件`RefreshRemoteApplicationEvent`

```java
public class RefreshRemoteApplicationEvent extends RemoteApplicationEvent {

	@SuppressWarnings("unused")
	private RefreshRemoteApplicationEvent() {
		// for serializers
	}

	public RefreshRemoteApplicationEvent(Object source, String originService,
			String destinationService) {
		super(source, originService, destinationService);
	}
}
```

## 自定义监听器,监听同一个事件`RefreshRemoteApplicationEvent`

```java
@Slf4j
@Configuration
public class BusConfiguration {

    /**
     * 监听特定事件{@link RefreshRemoteApplicationEvent}
     *
     * @param event
     */
    @EventListener
    public void onRefreshRemoteApplicationEvent(RefreshRemoteApplicationEvent event) {
        log.info("source: {}, originService: {}, destinationService: {}", event.getSource(), event.getOriginService(), event.getDestinationService());
    }
}
```

## 激活bud信息跟踪

```properties
spring.cloud.bus.trace.enabled=true
```

## 查看信息`http://localhost:9011/trace`

```json
[{
	"timestamp": "2019-04-01T08:03:04.649+0000",
	"info": {
		"signal": "spring.cloud.bus.ack",
		"event": "EnvironmentChangeRemoteApplicationEvent",
		"id": "64f0ffab-a3f4-4adf-bfe8-08e6e710b1c4",
		"origin": "spring-cloud-ribbon-client:test:8089",
		"destination": "**"
	}
}, {
	"timestamp": "2019-04-01T08:02:42.893+0000",
	"info": {
		"signal": "spring.cloud.bus.sent",
		"type": "EnvironmentChangeRemoteApplicationEvent",
		"id": "64f0ffab-a3f4-4adf-bfe8-08e6e710b1c4",
		"origin": "spring-cloud-ribbon-client:test:8089",
		"destination": "**:**"
	}
}, {
	"timestamp": "2019-04-01T08:02:42.893+0000",
	"info": {
		"signal": "spring.cloud.bus.ack",
		"event": "EnvironmentChangeRemoteApplicationEvent",
		"id": "64f0ffab-a3f4-4adf-bfe8-08e6e710b1c4",
		"origin": "spring-cloud-user-server:test:8090",
		"destination": "**"
	}
}]
```

## 模拟三台客户端

- 第一台
  - 服务端口8089
  - 管理端口9010
- 第二台
  - 服务端口18089
  - 管理端口19010
- 第三台
  - 服务端口28089
  - 管理端口29010

## 广播方式发送事件

- 请求`curl -X POST http://192.168.0.97:9010/bus/refresh?destination=spring-cloud-ribbon-client:**`

- 查看结果

  - 第一台主机`<http://localhost:9010/trace>`

    ```json
    [{
    	"timestamp": 1554108594031,
    	"info": {
    		"signal": "spring.cloud.bus.ack",
    		"event": "RefreshRemoteApplicationEvent",
    		"id": "df3d8f7a-e5a5-4cf8-be79-4cd321a1062d",
    		"origin": "spring-cloud-ribbon-client:test:28089",
    		"destination": "spring-cloud-ribbon-client:**"
    	}
    }, {
    	"timestamp": 1554108589103,
    	"info": {
    		"signal": "spring.cloud.bus.ack",
    		"event": "RefreshRemoteApplicationEvent",
    		"id": "df3d8f7a-e5a5-4cf8-be79-4cd321a1062d",
    		"origin": "spring-cloud-ribbon-client:test:18089",
    		"destination": "spring-cloud-ribbon-client:**"
    	}
    }, {
    	"timestamp": 1554108586134,
    	"info": {
    		"signal": "spring.cloud.bus.sent",
    		"type": "RefreshRemoteApplicationEvent",
    		"id": "df3d8f7a-e5a5-4cf8-be79-4cd321a1062d",
    		"origin": "spring-cloud-ribbon-client:test:8089",
    		"destination": "spring-cloud-ribbon-client:**"
    	}
    }, {
    	"timestamp": 1554108586130,
    	"info": {
    		"signal": "spring.cloud.bus.ack",
    		"event": "RefreshRemoteApplicationEvent",
    		"id": "df3d8f7a-e5a5-4cf8-be79-4cd321a1062d",
    		"origin": "spring-cloud-ribbon-client:test:8089",
    		"destination": "spring-cloud-ribbon-client:**"
    	}
    }]
    ```

  - 第二台主机与第一台主机内容一样

  - 第三台主机与第一台主机内容一样

- 结论

  ```tex
  发送:第一台主机
  接收:第一台主机,第二台主机,第三台主机
  ```

## 单点方式发送事件

- 请求`curl -X POST http://192.168.0.97:9010/bus/refresh?destination=spring-cloud-user-server:prod:8090`

- 查看结果

  - 第一台主机`<http://localhost:9010/trace>`

    ```json
    {
    	timestamp: 1554109724077,
        info: {
            signal: "spring.cloud.bus.sent",
            type: "RefreshRemoteApplicationEvent",
            id: "89268ed0-7eac-4578-9a36-b49e62f349d8",
            origin: "spring-cloud-ribbon-client:test:8089",
            destination: "spring-cloud-user-server:prod:8090"
        }
    }
    ```

  - 第二台主机与第一台主机内容一样

  - 第三台主机与第一台主机内容一样

  - 消息接收端`spring-cloud-user-server`结果

    ```json
    {
        timestamp: "2019-04-01T09:08:44.077+0000",
        info: {
            signal: "spring.cloud.bus.sent",
            type: "RefreshRemoteApplicationEvent",
            id: "89268ed0-7eac-4578-9a36-b49e62f349d8",
            origin: "spring-cloud-ribbon-client:test:8089",
            destination: "spring-cloud-user-server:prod:8090"
        }
    }
    ```

- 结论

  ```tex
  发送:第一台主机
  接收:第一台主机,第二台主机,第三台主机
  ```

  