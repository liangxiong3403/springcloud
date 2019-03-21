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

## 通过代理访问服务端或客户端接口

- 代理服务端访问

> `http://localhost:8092/spring-cloud-user-server/feign/users/`
>
> `http://localhost:8092/spring-cloud-user-server/feign/users/1`

- 代理客户端访问

> `http://localhost:8092/spring-cloud-ribbon-client/ribbon/remote/users/`
>
> `http://localhost:8092/spring-cloud-ribbon-client/feign/users/`

## 获取代理的路由列表

> `http://localhost:9013/routes`

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

# 客户端

## 默认客户端

- 默认客户端

  > `org.apache.http.client.HttpClient`

- 默认装配类

  > `org.springframework.cloud.netflix.ribbon.apache.HttpClientRibbonConfiguration`

## 自定义客户端

- 自定义客户端

  > `okhttp3.OkHttpClient`

- 自动装配类

  > `org.springframework.cloud.netflix.ribbon.okhttp.OkHttpRibbonConfiguration`

- 配置项

  > ```
  > ribbon.okhttp.enabled=true
  > ```

# ZuulFilter

- 查看类的文档注释一段话

  > Any filterType made be created or added and run by calling FilterProcessor.runFilters(type)

- 查看`com.netflix.zuul.FilterProcessor#runFilters`

  ```java
  public Object runFilters(String sType) throws Throwable {
      if (RequestContext.getCurrentContext().debugRouting()) {
          Debug.addRoutingDebug("Invoking {" + sType + "} type filters");
      }
      boolean bResult = false;
      List<ZuulFilter> list = FilterLoader.getInstance().getFiltersByType(sType);
      if (list != null) {
          for (int i = 0; i < list.size(); i++) {
              ZuulFilter zuulFilter = list.get(i);
              // 下一次调用
              Object result = processZuulFilter(zuulFilter);
              if (result != null && result instanceof Boolean) {
                  bResult |= ((Boolean) result);
              }
          }
      }
      return bResult;
  }
  ```

- 处理filter(`com.netflix.zuul.FilterProcessor#processZuulFilter`)

  ```java
   public Object processZuulFilter(ZuulFilter filter) throws ZuulException {
  
          RequestContext ctx = RequestContext.getCurrentContext();
          boolean bDebug = ctx.debugRouting();
          final String metricPrefix = "zuul.filter-";
          long execTime = 0;
          String filterName = "";
          try {
              long ltime = System.currentTimeMillis();
              filterName = filter.getClass().getSimpleName();
              
              RequestContext copy = null;
              Object o = null;
              Throwable t = null;
  
              if (bDebug) {
                  Debug.addRoutingDebug("Filter " + filter.filterType() + " " + filter.filterOrder() + " " + filterName);
                  copy = ctx.copy();
              }
              // 调用接口ZuulFilter的方法
              ZuulFilterResult result = filter.runFilter();
              ExecutionStatus s = result.getStatus();
              execTime = System.currentTimeMillis() - ltime;
          }
       // 省略代码...
   }
  ```

- 调用ZuulFilter的`runFilter`方法

  ```java
  public ZuulFilterResult runFilter() {
      ZuulFilterResult zr = new ZuulFilterResult();
      if (!isFilterDisabled()) {
          if (shouldFilter()) {
              Tracer t = TracerFactory.instance().startMicroTracer("ZUUL::" + this.getClass().getSimpleName());
              try {
                  // 调用实现类方法
                  Object res = run();
                  zr = new ZuulFilterResult(res, ExecutionStatus.SUCCESS);
              } catch (Throwable e) {
                  t.setName("ZUUL::" + this.getClass().getSimpleName() + " failed");
                  zr = new ZuulFilterResult(ExecutionStatus.FAILED);
                  zr.setException(e);
              } finally {
                  t.stopAndLog();
              }
          } else {
              zr = new ZuulFilterResult(ExecutionStatus.SKIPPED);
          }
      }
      return zr;
  }
  ```

- 调用实现类的`run`方法

  ```java
  public Object run() {
      RequestContext context = RequestContext.getCurrentContext();
      this.helper.addIgnoredHeaders();
      try {
          RibbonCommandContext commandContext = buildCommandContext(context);
          ClientHttpResponse response = forward(commandContext);
          setResponse(response);
          return response;
      }
      catch (ZuulException ex) {
          throw new ZuulRuntimeException(ex);
      }
      catch (Exception ex) {
          throw new ZuulRuntimeException(ex);
      }
  }
  ```

# `@EnableZuulProxy`处理逻辑

## `@EnableZuulProxy`声明代理类

```java
@EnableZuulProxy
@SpringBootApplication
public class ZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}
```

## `ZuulProxyMarkerConfiguration.Marker`初始化

```java
@Configuration
public class ZuulProxyMarkerConfiguration {
	@Bean
	public Marker zuulProxyMarkerBean() {
		return new Marker();
	}

	class Marker {
	}
}
```

## 触发自动装配ZuulFilter(将会集成父类Bean)

```java
@Configuration
@Import({ RibbonCommandFactoryConfiguration.RestClientRibbonConfiguration.class,
		RibbonCommandFactoryConfiguration.OkHttpRibbonConfiguration.class,
		RibbonCommandFactoryConfiguration.HttpClientRibbonConfiguration.class,
		HttpClientConfiguration.class })
@ConditionalOnBean(ZuulProxyMarkerConfiguration.Marker.class)
public class ZuulProxyAutoConfiguration extends ZuulServerAutoConfiguration {
    @Bean
	public RibbonRoutingFilter ribbonRoutingFilter(ProxyRequestHelper helper,
			RibbonCommandFactory<?> ribbonCommandFactory) {
		RibbonRoutingFilter filter = new RibbonRoutingFilter(helper, ribbonCommandFactory,
				this.requestCustomizers);
		return filter;
	}
}
```

## 初始化控制器和ZuulServlet(这部分Bean继承自父类)

```java
@Configuration
@EnableConfigurationProperties({ ZuulProperties.class })
@ConditionalOnClass(ZuulServlet.class)
@ConditionalOnBean(ZuulServerMarkerConfiguration.Marker.class)
// Make sure to get the ServerProperties from the same place as a normal web app would
@Import(ServerPropertiesAutoConfiguration.class)
public class ZuulServerAutoConfiguration {
    @Bean
	public ZuulController zuulController() {
		return new ZuulController();
	}

	@Bean
	public ZuulHandlerMapping zuulHandlerMapping(RouteLocator routes) {
		ZuulHandlerMapping mapping = new ZuulHandlerMapping(routes, zuulController());
		mapping.setErrorController(this.errorController);
		return mapping;
	}
    /*
     *com.netflix.zuul.http.ZuulServlet自动实例化
     *
     */
    @Bean
	@ConditionalOnMissingBean(name = "zuulServlet")
	public ServletRegistrationBean zuulServlet() {
        // 设置了映射地址为/zuul/*
        /* 
         * 所以http://localhost:8092/zuul/spring-cloud-ribbon-client/feign/users/等同于
         * http://localhost:8092/spring-cloud-ribbon-client/feign/users/
         */
		ServletRegistrationBean servlet = new ServletRegistrationBean(new ZuulServlet(),
				this.zuulProperties.getServletPattern());
		servlet.addInitParameter("buffer-requests", "false");
		return servlet;
	}
    /*
     * 省略代码....
     *
     */
}
```

# `@EnableZuulServer `处理逻辑

## `@EnableZuulServer`声明服务

```java
@EnableZuulServer
@SpringBootApplication
public class ZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}
```

## `ZuulServerMarkerConfiguration.Marker`初始化

```java
@Configuration
public class ZuulServerMarkerConfiguration {
	@Bean
	public Marker zuulServerMarkerBean() {
		return new Marker();
	}

	class Marker {
	}
}
```

## 触发自动装配

```java
@Configuration
@EnableConfigurationProperties({ ZuulProperties.class })
@ConditionalOnClass(ZuulServlet.class)
@ConditionalOnBean(ZuulServerMarkerConfiguration.Marker.class)
// Make sure to get the ServerProperties from the same place as a normal web app would
@Import(ServerPropertiesAutoConfiguration.class)
public class ZuulServerAutoConfiguration {
    @Bean
	public ZuulController zuulController() {
		return new ZuulController();
	}

	@Bean
	public ZuulHandlerMapping zuulHandlerMapping(RouteLocator routes) {
		ZuulHandlerMapping mapping = new ZuulHandlerMapping(routes, zuulController());
		mapping.setErrorController(this.errorController);
		return mapping;
	}
    /*
     *com.netflix.zuul.http.ZuulServlet自动实例化
     *
     */
    @Bean
	@ConditionalOnMissingBean(name = "zuulServlet")
	public ServletRegistrationBean zuulServlet() {
		ServletRegistrationBean servlet = new ServletRegistrationBean(new ZuulServlet(),
				this.zuulProperties.getServletPattern());
		// The whole point of exposing this servlet is to provide a route that doesn't
		// buffer requests.
		servlet.addInitParameter("buffer-requests", "false");
		return servlet;
	}
    /*
     * 省略代码....
     *
     */
}
```

## org.springframework.cloud.netflix.zuul.web.ZuulController

```java
public class ZuulController extends ServletWrappingController {

	public ZuulController() {
        // 设置ZuulServlet
		setServletClass(ZuulServlet.class);
		setServletName("zuul");
		setSupportedMethods((String[]) null); // Allow all
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
            // 委派请求给ZuulServlet处理
			return super.handleRequestInternal(request, response);
		}
		finally {
			RequestContext.getCurrentContext().unset();
		}
	}

}
```

- org.springframework.web.servlet.mvc.ServletWrappingController#handleRequestInternal

- com.netflix.zuul.http.ZuulServlet#service

- com.netflix.zuul.ZuulRunner#preRoute
  - com.netflix.zuul.FilterProcessor#preRoute
  - com.netflix.zuul.FilterProcessor#route
  - com.netflix.zuul.FilterProcessor#postRoute
  - com.netflix.zuul.FilterProcessor#error(发生异常时调用)

- com.netflix.zuul.FilterProcessor#runFilters

- com.netflix.zuul.FilterProcessor#processZuulFilter

- com.netflix.zuul.ZuulFilter#runFilter

- 调用实现类`run`方法`org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter#run`

  ```java
  public Object run() {
      RequestContext context = RequestContext.getCurrentContext();
      this.helper.addIgnoredHeaders();
      try {
          RibbonCommandContext commandContext = buildCommandContext(context);
          ClientHttpResponse response = forward(commandContext);
          setResponse(response);
          return response;
      }
      catch (ZuulException ex) {
          throw new ZuulRuntimeException(ex);
      }
      catch (Exception ex) {
          throw new ZuulRuntimeException(ex);
      }
  }
  ```

# `EnableZuulProxy` 与`EnableZuulServer`区别的官方说明

> Spring Cloud Netflix installs a number of filters based on which annotation was used to enable Zuul. `@EnableZuulProxy` is a superset of `@EnableZuulServer`. In other words, `@EnableZuulProxy` contains all filters installed by `@EnableZuulServer`. The additional filters in the "proxy" enable routing functionality. If you want a "blank" Zuul, you should use `@EnableZuulServer`.

