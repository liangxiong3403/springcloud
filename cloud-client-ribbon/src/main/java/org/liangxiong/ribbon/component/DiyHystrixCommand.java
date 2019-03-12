package org.liangxiong.ribbon.component;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        super(HystrixCommandGroupKey.Factory.asKey(groupName), 3000);
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
        // 模拟客户端超时
        int second = random.nextInt(5);
        log.info("client execution time: {}", second);
        TimeUnit.SECONDS.sleep(second);
        return restTemplate.postForObject(url.toString(), params, HashMap.class);
    }

    /**
     * 回调方法用于熔断恢复
     */
    @Override
    public Object getFallback() {
        return Collections.emptyMap();
    }
}
