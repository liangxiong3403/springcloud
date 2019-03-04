# Spring Cloud配置服务器

## 搭建Spring Cloud Config Server

### 基于本地文件系统

- 创建本地文件目录

> src/main/resources/config

- 创建配置文件

```properties
user.properties
user-prod.properties
user-test.properties
```

- 配置本地目录(参考org.springframework.cloud.config.server.config.ConfigServerProperties)

```yaml
server:
    port: 8082
management:
    security:
        enabled: false
    port: 9003
spring:
    application:
        name: spring-cloud-config-server
    cloud:
        config:
            server:
                git:
                    # Spring Cloud配置服务器仓库地址
                    uri: ${user.dir}\cloud-config-server\src\main\resource\config
```

- 使用@EnableConfigServer激活应用配置服务器

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ConfigServerApplication.class);
        springApplication.run(args);
    }

}
```

- 出现异常

  - 访问http://localhost:8082/user/prod
  - 报错

  ```tex
  This application has no explicit mapping for /error, so you are seeing this as a fallback.
  
  Sun Dec 23 19:59:02 CST 2018
  There was an unexpected error (type=Not Found, status=404).
  Cannot clone or checkout repository: E:\development\idea_project\springcloud\\cloud-config-server\\src\\main\\resources\\config
  ```

- 解决异常

```shell
cd E:\development\idea_project\springcloud\cloud-config-server\src\main\resources\config
git init
git add .
git commit -m "cloud config init"
```

- 测试配置服务器

```tex
http://localhost:8082/user/dev
http://localhost:8082/user/prod
http://localhost:8082/user/test
```

- 当指定profifle时,默认地profile信息也会展示(比如下面的user.properties部分内容)

```json
{
	"name": "user",
	"profiles": ["prod"],
	"label": null,
	"version": "491d4075e85e001491e180dabf30468b1594ffc4",
	"state": null,
	"propertySources": [{
		"name": "E:\\development\\idea_project\\springcloud\\\\cloud-config-server\\\\src\\\\main\\\\resources\\\\config/user-prod.properties",
		"source": {
			"name": "user-prod"
		}
	}, {
		"name": "E:\\development\\idea_project\\springcloud\\\\cloud-config-server\\\\src\\\\main\\\\resources\\\\config/user.properties",
		"source": {
			"name": "user"
		}
	}]
}
```

### 基于远程Github仓库

- Github上面创建临时仓库temp,用于存放配置文件

```tex
https://github.com/liangxiong3403/temp
```

- 提交一些配置文件到Github

```tex
user.properties
user-prod.properties
user-test.properties
```

- 服务器配置Github地址

```properties
server:
    port: 8082
management:
    security:
        enabled: false
    port: 9003
spring:
    application:
        name: spring-cloud-config-server
    cloud:
        config:
            server:
                git:
                    # 配置远程Github仓库地址
                    uri: https://github.com/liangxiong3403/temp
```

- 配置强制拉取仓库最新内容

```yaml
#Flag to indicate that the repository should force pull. If true discard any local changes and take from remote repository.
spring:
    cloud:
        config:
            server:
                git:
                    force-pull: true
```



- 重启应用服务器
- 测试配置服务器

```tex
http://localhost:8082/user/dev
http://localhost:8082/user/prod
http://localhost:8082/user/test
```

**结果**

```json
{
	"name": "user",
	"profiles": ["test"],
	"label": null,
	"version": "4d2fb52faa76c8c2794046b3b5976b7bdb2dd63a",
	"state": null,
	"propertySources": [{
		"name": "https://github.com/liangxiong3403/temp/user-test.properties",
		"source": {
			"name": "user-test-v2"
		}
	}, {
		"name": "https://github.com/liangxiong3403/temp/user.properties",
		"source": {
			"name": "user"
		}
	}]
}
```

- 查看配置文件内容

```tex
http://localhost:8082/user-default.properties
http://localhost:8082/user-prod.properties
http://localhost:8082/user-test.properties
```

# Spring Cloud配置客户端

## 搭建 Spring Cloud Config Client

- 配置application.yml

```yaml
server:
    port: 8081
management:
    security:
        enabled: false
    port: 9002
spring:
    application:
        name: spring-cloud-config-client
```

- bootstrap.properties(参考参考org.springframework.cloud.config.client.ConfigClientProperties)

```yaml
spring:
    cloud:
        config:
			# 配置服务器地址
            uri: http://127.0.0.1:8082
            # 拉取地配置文件所属地应用名称(optional)
            name: user
            # 拉取地配置文件所属地应用环境(开发/测试/生产)
            profile: test
            # 拉取地配置文件所属地标签
            label: master
```

- 查看客户端启动日志

  > 2019-03-04 10:45:54.546 | INFO  | restartedMain | org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration | Located property source: CompositePropertySource [name='configService', propertySources=[MapPropertySource {name='configClient'}, MapPropertySource {name='https://github.com/liangxiong3403/temp/user-test.properties'}, MapPropertySource {name='https://github.com/liangxiong3403/temp/user.properties'}]]

- 如果没有指定spring.cloud.config.name,那么会使用spring.application.name来作为application属性值

  > 2019-03-04 10:52:57.276 | INFO  | restartedMain | org.springframework.cloud.config.client.ConfigServicePropertySourceLocator | Located environment: name=spring-cloud-config-client, profiles=[test], label=master, version=41316b9ead54d5af90689422673988bf61b0ae63, state=null

- 查看actuator中的environment信息

  > configService:configClient: {
  > config.client.version: "41316b9ead54d5af90689422673988bf61b0ae63"
  > },
  > configService:https://github.com/liangxiong3403/temp/user-test.properties: {
  > name: "user-test"
  > },
  > configService:https://github.com/liangxiong3403/temp/user.properties: {
  > name: "user"
  > }

- 通过`http://localhost:9002/env/name`获取配置项名称

  > user-test

# 动态配置Bean

- 定义bean

```java
/**
 * @author liangxiong
 * @Date:2019-03-04
 * @Time:11:14
 * @Description 用户
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "user")
public class User {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 年龄
     */
    private Integer age;
}
```

- 注册bean

```java
/**
 * @author liangxiong
 * @Date:2019-03-04
 * @Time:11:20
 * @Description 主页控制器
 */
@RestController
@EnableConfigurationProperties(User.class)
@RequestMapping("/index")
public class IndexController {

    private final User user;

    @Autowired
    public IndexController(User user) {
        this.user = user;
    }

    @GetMapping("/config/user")
    public User getUserFromConfig() {
        return user;
    }

}
```

- 通过`http://localhost:8081/index/config/user`获取返回值

```json
{
    "nickname": "baoyatou",
    "age": 16
}
```

- 通过post方法修改数据,localhost:9002/env?user.age=20&user.sex=female&user.nickname=xuebaochai

- 查看`http://localhost:8081/index/config/user`返回值

```json
{
    "nickname": "xuebaochai",
    "age": 20
}
```

# 批量修改配置文件

> 配置客户端拉取地配置文件优先级高于默认地application.properties或application.yml或bootstrap.properties

- 修改配置服务器对应配置文件`user-test.properties`

```properties
# 应用名称user,测试环境
name=user-test
user.nickname=xuebaochai
user.age=18
```

- 提交配置信息

```shell
git add .
git commit -m "update server's test config file"
git push
```

- 重启客户端

>  发现所有user相关配置都已经修改了(因为服务端配置优先级更高)

- 通过调用**客户端**的`特定`endpoint,客户端获取配置**服务端**动态修改地内容(无需重启客户端)

```shell
POST localhost:9002/refresh
```

> 通过调用refresh接口,实际上访问了org.springframework.cloud.endpoint.GenericPostableMvcEndpoint#invoke

- 动态感知配置服务器文件修改

  - 方式一:定时器refresh上下文

  ```java
  @Slf4j
  @SpringBootApplication
  public class ConfigClientApplication {
  
      private final ContextRefresher contextRefresher;
  
      @Autowired
      public ConfigClientApplication(ContextRefresher contextRefresher) {
          this.contextRefresher = contextRefresher;
      }
  
      public static void main(String[] args) {
          SpringApplication.run(ConfigClientApplication.class, args);
      }
  
      /**
       * 定时刷新配置服务器文件
       */
      @Scheduled(cron = "0/5 * * * * ? ")
      public void refreshServerConfig() {
          Set<String> keys = contextRefresher.refresh();
          if (!CollectionUtils.isEmpty(keys)) {
              log.info("server config changes: {}", keys);
          }
      }
  
  }
  
  
  ```

  - 方式二:通过官方提供地方式`spring-cloud-config-monitor`

  > You can configure the webhook via the provider’s user interface as a URL and a set of events in which you are interested. For instance Github will POST to the webhook with a JSON body containing a list of commits, and a header "X-Github-Event" equal to "push". If you add a dependency on the spring-cloud-config-monitor library and activate the Spring Cloud Bus in your Config Server, then a "/monitor" endpoint is enabled.

- 开启restart/pause/resume端点

```yaml
endpoints:
	# 同时开启restart/pause/resume
    restart:
        enabled: true
        sensitive: false
```

