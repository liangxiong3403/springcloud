package org.liangxiong.ribbon.ping;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * @author liangxiong
 * @Date:2019-03-11
 * @Time:15:00
 * @Description 自定义实现IPing规则
 */
public class DiyPingImpl implements IPing {

    @Override
    public boolean isAlive(Server server) {
        String host = server.getHost();
        int port = server.getPort();
        // Spring 工具类构建
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.scheme("http");
        builder.host(host);
        builder.port(port);
        builder.path("/users");
        URI uri = builder.build().toUri();
        // 发送请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity responseEntity = restTemplate.getForEntity(uri, List.class);
        // 判断响应状态码
        return HttpStatus.OK.equals(responseEntity.getStatusCode());
    }
}
