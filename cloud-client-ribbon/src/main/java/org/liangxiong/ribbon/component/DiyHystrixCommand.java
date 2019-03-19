package org.liangxiong.ribbon.component;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

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
