package org.liangxiong.ribbon.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

    /**
     * 添加用户
     *
     * @param params
     * @return
     */
    @PostMapping("/remote/users")
    public Object getRemoteUser(@RequestBody JSONObject params) {
        StringBuffer url = new StringBuffer();
        // 方式一
        ///url.append("http://").append(remoteServiceProviderHost).append(":").append(remoteServiceProviderPort).append("/users");
        // 方式二
        url.append("http://").append(remoteServiceProviderApplicationName).append("/users");
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
