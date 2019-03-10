package org.liangxiong.springcloud.config;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liangxiong
 * @Date:2018-12-22
 * @Time:21:01
 * @Description 自定义属性资源定位器(Spring Cloud标准方式)
 * @see CustomBootstrapConfiguration
 */
@Configuration
public class CustomPropertySourceLocator implements PropertySourceLocator {

    @Override
    public PropertySource<?> locate(Environment environment) {
        Map<String, String> data = new HashMap<>(8);
        data.put("lx.color", "blue");
        data.put("lx.gender", "male");
        // 代码类似于CustomBootstrapConfiguration的方式
        return new MapPropertySource("diyPropertyByCloud",
                Collections.unmodifiableMap(data));
    }
}
