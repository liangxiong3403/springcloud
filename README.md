Spring Cloud学习项目



# JDK版本

> 1.8

# Maven版本

> 3.5.3

# Spring Boot版本

> 1.5.18.RELEASE

# Spring Cloud版本

> Edgware.SR5

# SOA

> **Service-oriented architecture** (**SOA**) is a style of [software design](https://en.wikipedia.org/wiki/Software_design) where services are provided to the other components by [application components](https://en.wikipedia.org/wiki/Application_components), through a [communication protocol](https://en.wikipedia.org/wiki/Communications_protocol) over a network. The basic principles of service-oriented architecture are independent of vendors, products and technologies.A service is a discrete unit of functionality that can be accessed remotely and acted upon and updated independently, such as retrieving a credit card statement online.

# 微服务

> **Microservices** are a [software development](https://en.wikipedia.org/wiki/Software_development) technique—a variant of the [service-oriented architecture](https://en.wikipedia.org/wiki/Service-oriented_architecture) (SOA) architectural style that structures an [application](https://en.wikipedia.org/wiki/Application_(computing)) as a collection of [loosely coupled](https://en.wikipedia.org/wiki/Coupling_(computer_programming)) (松散耦合)services. In a microservices architecture, services are [fine-grained](https://en.wikipedia.org/wiki/Service_granularity_principle) and the [protocols](https://en.wikipedia.org/wiki/Protocol_(computing)) are lightweight. The benefit of decomposing(分解) an application into different smaller services is that it improves [modularity](https://en.wikipedia.org/wiki/Modular_programming). This makes the application easier to understand, develop, test, and become more resilient (有弹力)to architecture erosion(分解).It parallelizes [development](https://en.wikipedia.org/wiki/Software_development) by enabling small autonomous(自治) teams to develop, [deploy](https://en.wikipedia.org/wiki/Software_deployment) and scale their respective services independently.It also allows the architecture of an individual service to emerge(展现) through continuous [refactoring](https://en.wikipedia.org/wiki/Refactoring).Microservices-based architectures enable [continuous delivery](https://en.wikipedia.org/wiki/Continuous_delivery) and deployment.

# Endpoint

- org.springframework.cloud.context.restart.RestartEndpoint
- org.springframework.cloud.context.restart.RestartEndpoint.PauseEndpoint
- org.springframework.cloud.context.restart.RestartEndpoint.ResumeEndpoint

# Event

- java.util.EventObject
  - org.springframework.context.ApplicationEvent

# Listener

- java.util.EventListener

  - org.springframework.context.ApplicationListener

    - org.springframework.boot.context.config.ConfigFileApplicationListener

      - org.springframework.boot.context.config.ConfigFileApplicationListener.Loader#load()
      - org.springframework.boot.env.PropertySourcesLoader#load(org.springframework.core.io.Resource, java.lang.String, java.lang.String, java.lang.String)

      > 读取SpringBoot配置文件
      >
      > 优先级:Ordered.HIGHEST_PRECEDENCE + 10

    - org.springframework.cloud.bootstrap.BootstrapApplicationListener

      > 读取SpringCloud配置文件
      >
      > 优先级:DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 5
      >
      > BootstrapApplicationListener加载优先级高于SpringBoot的ConfigFileApplicationListener

    - org.springframework.boot.SpringApplication#prepareEnvironment方法调用

      > 将会触发ApplicationEnvironmentPreparedEvent事件,从而被org.springframework.cloud.bootstrap.BootstrapApplicationListener监听到,从而创建SpringCloud上下文

    - org.springframework.boot.SpringApplication#createApplicationContext方法创建SpringBoot应用上下文

      > ApplicationEnvironmentPreparedEvent事件也将触发ConfigFileApplicationListener监听器

# PropertySourceLoader

- org.springframework.boot.env.PropertySourceLoader
  - org.springframework.boot.env.PropertiesPropertySourceLoader
  - org.springframework.boot.env.YamlPropertySourceLoader

# ApplicationContext

- org.springframework.context.ApplicationContext
  - org.springframework.context.ConfigurableApplicationContext
    - org.springframework.context.annotation.AnnotationConfigApplicationContext
    - org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext

# Environment

- org.springframework.core.env.Environment

  - org.springframework.core.env.AbstractEnvironment

    - org.springframework.core.env.StandardEnvironment
    - org.springframework.web.context.support.StandardServletEnvironment
    - org.springframework.mock.env.MockEnvironment

  - org.springframework.core.env.ConfigurableEnvironment

    ```java
    void setActiveProfiles(String... profiles);
    void addActiveProfile(String profile);
    void setDefaultProfiles(String... profiles);
    MutablePropertySources getPropertySources();
    Map<String, Object> getSystemProperties();
    Map<String, Object> getSystemEnvironment();
    void merge(ConfigurableEnvironment parent);
    ```

# PropertySource

- org.springframework.core.env.ConfigurableEnvironment

  ```java
  MutablePropertySources getPropertySources();
  ```

- org.springframework.core.env.MutablePropertySources implements PropertySources 

- org.springframework.core.env.PropertySources extends Iterable<PropertySource<?>>

- org.springframework.core.env.PropertySource

  > key就是env接口返回的JSON数据的key
  >
  > value是泛型

  - org.springframework.core.env.MapPropertySource
  - org.springframework.core.env.PropertiesPropertySource
  - org.springframework.core.env.CommandLinePropertySource

# 注意事项

> 由于BootstrapApplicationListener优先于ConfigFileApplicationListener加载,所以不能通过SpringBoot默认配置文件去修改SpringCloud`特定`地属性

# 修改SpringCloud配置(通过命令行)

- 关闭Bootstrap上下文

> --spring.cloud.bootstrap.enabled=false

- 指定配置文件名称

> --spring.cloud.bootstrap.name=spring-cloud

- 指定配置文件位置(默认位置为classpath根路径,同时需要指定配置文件名称)

> --spring.cloud.bootstrap.location=classpath:cloud/spring-cloud.yml

# 命令行读取参数

- org.springframework.boot.SpringApplication

  - 第一步

  ```java
  public static ConfigurableApplicationContext run(Object source, String... args) {
  		return run(new Object[] { source }, args);
  }
  ```

  - 第二步

  ```java
  public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
  		return new SpringApplication(sources).run(args);
  }
  ```

  - 第三步

  ```java
  public ConfigurableApplicationContext run(String... args) {
  		StopWatch stopWatch = new StopWatch();
  		stopWatch.start();
  		ConfigurableApplicationContext context = null;
  		FailureAnalyzers analyzers = null;
  		configureHeadlessProperty();
  		SpringApplicationRunListeners listeners = getRunListeners(args);
  		listeners.starting();
  		try {
  			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
  					args);
  			ConfigurableEnvironment environment = prepareEnvironment(listeners,
  					applicationArguments);
  			Banner printedBanner = printBanner(environment);
              // 省略代码...
             	if (this.logStartupInfo) {
                      new StartupInfoLogger(this.mainApplicationClass)
  						.logStarted(getApplicationLog(), stopWatch);
  			}
  			return context;
          } catch (Throwable ex) {
  			handleRunFailure(context, listeners, analyzers, ex);
  			throw new IllegalStateException(ex);
  		}
   }
  ```

  - 第四步

  ```java
  private ConfigurableEnvironment prepareEnvironment(
  			SpringApplicationRunListeners listeners,
  			ApplicationArguments applicationArguments) {
      // Create and configure the environment
      ConfigurableEnvironment environment = getOrCreateEnvironment();
      configureEnvironment(environment, applicationArguments.getSourceArgs());
      listeners.environmentPrepared(environment);
      if (!this.webEnvironment) {
          environment = new EnvironmentConverter(getClassLoader())
              .convertToStandardEnvironmentIfNecessary(environment);
      }
      return environment;
  }
  ```

  - 第五步

  ```java
  protected void configureEnvironment(ConfigurableEnvironment environment,
  			String[] args) {
      configurePropertySources(environment, args);
      configureProfiles(environment, args);
  }
  ```

  - 第六步

  ```java
  protected void configurePropertySources(ConfigurableEnvironment environment,
  			String[] args) {
  		MutablePropertySources sources = environment.getPropertySources();
  		if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
              sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
          }
      if (this.addCommandLineProperties && args.length > 0) {
          String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
          if (sources.contains(name)) {
              PropertySource<?> source = sources.get(name);
              CompositePropertySource composite = new CompositePropertySource(name);
              composite.addPropertySource(new SimpleCommandLinePropertySource(
                  name + "-" + args.hashCode(), args));
              composite.addPropertySource(source);
              sources.replace(name, composite);
          }
          else {
              sources.addFirst(new SimpleCommandLinePropertySource(args));
          }
      }
  }
  ```

# 覆盖远程属性(Overriding the Values of Remote Properties)

- SpringCloud默认开启了远程属性覆盖

> spring.cloud.config.allowOverride=true

- 通过Spring原生方式

```java
@Configuration
public class CustomBootstrapConfiguration implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // 获取环境
        ConfigurableEnvironment environment = context.getEnvironment();
        // 获取属性资源
        MutablePropertySources propertySources = environment.getPropertySources();
        // 添加自定义属性
        Map<String, Object> source = new HashMap<>(8);
        source.put("lx.name", "xiangqian");
        source.put("lx.sex", "male");
        source.put("lx.age", 25);
        PropertySource mapPropertySource = new MapPropertySource("diyPersonBySpring", source);
        // 添加到指定属性资源key地前面(通过env接口查看key)
        propertySources.addBefore("applicationConfig: [classpath:/bootstrap.yml]", mapPropertySource);
    }
}
```

- 通过SpringCloud方式

```java
@Configuration
public class CustomPropertySourceLocator implements PropertySourceLocator {

    @Override
    public PropertySource<?> locate(Environment environment) {
        // 代码类似于CustomBootstrapConfiguration的方式
        return new MapPropertySource("diyPropertyByCloud",
                Collections.singletonMap("lx.color", "blue"));
    }
}
```

- 配置spring.factories

```properties
org.springframework.cloud.bootstrap.BootstrapConfiguration=\
org.liangxiong.springcloud.config.CustomBootstrapConfiguration,\
org.liangxiong.springcloud.config.CustomPropertySourceLocator
```

# 项目组合关系

## Spring Cloud配置入门

- cloud-common

## 普通配置服务器和客户端

- cloud-config-server-git
- cloud-config-client-git

## Eureka客户端和服务端

- cloud-server-eureka
- cloud-client-eureka
- cloud-config-server-as-client-for-eureka(也是作为eureka客户端,同时作为配置中心)

## Eureka高可用

- cloud-client-eureka-high-available
- cloud-server-eureka-high-available

## Spring Cloud Consul与Eureka客户端集成

- cloud-client-consul
- consul本地安装应用程序(hostname:localhost,port:8500)

## 负载均衡

- cloud-server-eureka(注册中心)
- cloud-client-ribbon
- cloud-server-user(服务提供方)