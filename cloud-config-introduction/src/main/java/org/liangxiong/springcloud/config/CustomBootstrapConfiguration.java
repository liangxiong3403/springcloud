package org.liangxiong.springcloud.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liangxiong
 * @Date:2018-12-22
 * @Time:20:45
 * @Description 自定义启动配置类(Spring标准方式)
 * @see CustomPropertySourceLocator
 */
@Configuration
public class CustomBootstrapConfiguration implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // 获取环境
        ConfigurableEnvironment environment = context.getEnvironment();
        // 获取属性资源
        MutablePropertySources propertySources = environment.getPropertySources();
        // 添加自定义属性
        Map<String, Object> source = new HashMap<>(8);
        source.put("lx.name", "xiangqian");
        source.put("lx.sex", "male");
        source.put("lx.age", 25);
        PropertySource propertySource = new MapPropertySource("diyPersonBySpring", source);
        // 添加到指定属性资源key地前面(通过env接口查看key)
        propertySources.addBefore("applicationConfig: [classpath:/bootstrap.yml]", propertySource);
    }
}
