package org.liangxiong.consul.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link DiscoveryClient} {@link RestController}
 *
 * @author liangxiong
 * @Date:2019-03-08
 * @Time:16:55
 * @Description DiscoveryClient实现
 */
@RestController
public class DiscoveryClientController {

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String currentApplicationName;

    public DiscoveryClientController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 获取所有服务实例名称列表
     *
     * @return
     */
    @GetMapping("/discovery/services")
    public List<String> getAllServices() {
        return discoveryClient.getServices();
    }

    /**
     * 获取当前实例信息
     *
     * @return
     */
    @GetMapping("/discovery/service-current")
    public ServiceInstance getCurrentApplicationInfo() {
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(currentApplicationName);
        if (!CollectionUtils.isEmpty(serviceInstances)) {
            return serviceInstances.get(0);
        }
        return null;
    }

    /**
     * 获取所有服务实例信息
     */
    @GetMapping("/discovery/instances")
    public List<ServiceInstance> getAllApplicationInfo() {
        List<String> serviceNames = getAllServices();
        List<ServiceInstance> serviceInstances = new LinkedList<>();
        serviceNames.forEach(serviceName ->
                serviceInstances.addAll(discoveryClient.getInstances(serviceName))
        );
        return serviceInstances;
    }
}
