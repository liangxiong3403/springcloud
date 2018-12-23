# Spring Cloud配置服务器

## 搭建Spring Cloud Config Server

### 基于Git

- 创建本地文件目录

> src/main/resources/config

- 创建配置文件

```properties
user.properties
user-prod.properties
user-test.properties
```

- 配置本地目录

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

- 激活应用配置服务器

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

