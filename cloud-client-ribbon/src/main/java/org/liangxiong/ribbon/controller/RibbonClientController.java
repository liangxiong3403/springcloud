package org.liangxiong.ribbon.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

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
        return restTemplate.postForObject(url.toString(), params, HashMap.class);
    }

}
