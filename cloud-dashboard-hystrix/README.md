# CircuitBreaker(断路器)

> 概念来源于
>
> [Martin Fowler]: https://martinfowler.com/bliki/CircuitBreaker.html

- 名词解释

> 对于软件系统来说,对于运行于不同进程,可能是网络中不同主机之上地软件进行远程调用是很常见地.内存调用和远程调用最大区别之一就是:远程调用可能失败,或者保持挂起直到超时发生.更糟糕地是,当你在一个没有响应地提供方上存在多个调用者,你可能耗光资源从而导致多个系统之间地级联错误.
>
> 短路器的基本思想非常简单,你在断路器对象中包装一个受保护地功能调用,断路器对象监控失败.一旦失败到达特定阈值,断路器就断开,所有对断路器的调用返回错误,而不会调用受保护地功能.通常你也希望一些类型监视器在断路器断开后发送报警.

- 传统异常处理方式

```java
package org.liangxiong.dashboard.hystrix.config;

/**
 * @author liangxiong
 * @Date:2019-03-12
 * @Time:10:34
 * @Description 异常处理配置类
 */
@RestControllerAdvice(assignableTypes = {IndexController.class})
public class ExceptionConfiguration {

    @ExceptionHandler(TimeoutException.class)
    public String handlerControllerException(TimeoutException e) {
        return e.getMessage();
    }
}
```

# 服务端配置Hystrix

## `cloud-server-user`项目添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

## `@EnableHystrix`激活`hystrix服务端`

```java
package org.liangxiong.server.provider;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:04
 * @Description 用户服务提供者, Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
 */
@EnableHystrix
@EnableDiscoveryClient
@SpringBootApplication
public class UserProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApplication.class, args);
    }
}
```

## `@HystrixCommand`设置服务提供方的超时配置

```java
package org.liangxiong.server.provider.controller;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:06
 * @Description 用户服务provider
 */
@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private IUserService userService;

    private static Random random = new Random();

    /**
     * 添加用户
     *
     * @param user 用户实体
     * @return
     */
    @PostMapping
    public Object addUser(@RequestBody User user) {
        JSONObject result = new JSONObject(8);
        result.put("name", user.getUsername());
        result.put("age", user.getAge());
        result.put("userId", user.getUserId());
        // 区分服务端
        result.put("serverPort", port);
        return userService.addUser(user) ? result : new HashMap<Integer, Object>(8);
    }

    /**
     * 获取所有用户数据
     * <p>
     * 使用HystrixProperty设置超时时间
     *
     * @return
     */
    @HystrixCommand(fallbackMethod = "listAllUserFallback", commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")})
    @GetMapping
    public List<User> listAllUser() {
        // 模拟超时
        timeout();
        return userService.listAllUsers();
    }

    /**
     * 超时发生以后地回调方法
     *
     * @return
     */
    private List<User> listAllUserFallback() {
        return Collections.emptyList();
    }

    /**
     * 模拟超时
     */
    private void timeout() {
        // 模拟超时
        try {
            int second = random.nextInt(3);
            log.info("server execution time: {}", second);
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            log.error("thread interruption: {}", e.getMessage());
        }
    }
}
```

# 客户端配置Hystrix

## `cloud-client-ribbon`项目添加依赖

```xml
<!-- Hystrix相关依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

## `@EnableCircuitBreaker`激活断路器

```java
package org.liangxiong.ribbon;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:9:50
 * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端,Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
 */
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

## 通过编程方式进行短路实现

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

    private static Random random = new Random();

    /**
     * 前端参数
     */
    private JSONObject params;

    public DiyHystrixCommand(String groupName, String remoteServiceProviderApplicationName, RestTemplate restTemplate) {
        super(HystrixCommandGroupKey.Factory.asKey(groupName), 20);
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
        StringBuffer url = new StringBuffer();
        url.append("http://").append(remoteServiceProviderApplicationName).append("/users");
        return restTemplate.postForObject(url.toString(), params, HashMap.class);
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

## 客户端进行远程方法调用

```java
package org.liangxiong.ribbon.controller;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:30
 * @Description ribbon作为客户端Controller
 */
@Slf4j
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

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    private DiyHystrixCommand diyHystrixCommand;

    @PostConstruct
    public void init() {
        this.diyHystrixCommand = new DiyHystrixCommand("spring-cloud-ribbon-client", remoteServiceProviderApplicationName, restTemplate);
    }

    /**
     * 添加用户
     *
     * @param params
     * @return
     */
    @PostMapping("/remote/users")
    public Object getRemoteUser(@RequestBody JSONObject params) {
        try {
            // 设置参数
            diyHystrixCommand.setParams(params);
            // 自定义hystrix的command实现
            return diyHystrixCommand.execute();
        } catch (Exception e) {
            log.error("remote call failure!");
        }
        return restTemplate.postForObject(url.toString(), params, HashMap.class);
    }

    /**
     * 获取所有用户
     *
     * @return
     */
    @GetMapping("/remote/users")
    public List<User> listAllUsers() {
        // 根据服务实例名称获取实例对象
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

}
```

## 客户端调用报错

```tex
com.netflix.hystrix.exception.HystrixRuntimeException: DiyHystrixCommand command executed multiple times - this is not permitted.
...
Caused by: java.lang.IllegalStateException: This instance can only be executed once. Please instantiate a new instance.
```

- 原因是:每个请求对应一个`新的`DiyHystrixCommand实例
- 解决报错

```java
package org.liangxiong.ribbon.controller;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:30
 * @Description ribbon作为客户端Controller
 */
@Slf4j
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
        try {
            // 解决调用报错
            this.diyHystrixCommand = new DiyHystrixCommand("spring-cloud-ribbon-client", remoteServiceProviderApplicationName, restTemplate);
            // 设置参数
            diyHystrixCommand.setParams(params);
            // 自定义hystrix的command实现
            return diyHystrixCommand.execute();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("remote call failure: {}", e.getMessage());
        }
        return null;
    }

}
```

# 配置Hystrix的Dashboard

## 项目`cloud-dashboard-hystrix`添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

## 激活dashboard

```java
package org.liangxiong.dashboard.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @author liangxiong
 * @Date:2019-03-12
 * @Time:9:42
 * @Description Hystrix Dashboard
 */
@EnableHystrixDashboard
@SpringBootApplication
public class HystrixDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboardApplication.class, args);
    }
}
```

## 项目配置文件

```yaml
server:
    port: 8091
management:
    port: 9012
    security:
        enabled: false
spring:
    application:
        name: spring-cloud-hystrix-dashboard
```

## dashboard访问页面

> `http://localhost:8091/hystrix `输入`http://localhost:9010/hystrix.stream`
