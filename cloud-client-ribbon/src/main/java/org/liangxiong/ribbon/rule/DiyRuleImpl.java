package org.liangxiong.ribbon.rule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * @author liangxiong
 * @Date:2019-03-11
 * @Time:14:21
 * @Description 自定义实现IRule规则
 */
@Component
public class DiyRuleImpl extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }

    @Override
    public Server choose(Object key) {
        Random random = new Random();
        // 获取负载均衡器
        ILoadBalancer loadBalancer = getLoadBalancer();
        // 获取可用服务列表
        List<Server> servers = loadBalancer.getReachableServers();
        if (!CollectionUtils.isEmpty(servers)) {
            // 随机获取一台可用服务
            return servers.get(random.nextInt(servers.size()));
        }
        return null;
    }
}
