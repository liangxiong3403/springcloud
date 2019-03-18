package org.liangxiong.ribbon;

import org.liangxiong.cloud.api.service.IUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:9:50
 * @Description 客户端负载均衡,@RibbonClient激活ribbon客户端,Edgware版本开始,@EnableEurekaClient或@EnableDiscoveryClient是非必需地
 * IUserService作为feign的客户端接口
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
