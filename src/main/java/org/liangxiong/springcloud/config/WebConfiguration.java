package org.liangxiong.springcloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author liangxiong
 * @Date:2018-12-22
 * @Time:21:08
 * @Description Web相关配置
 */
@Configuration
public class WebConfiguration {

    /**
     * 跨域请求配置
     *
     * @return
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 那些地址需要跨域处理
                registry.addMapping("/**")
                    // 那些origin需要跨域处理
                    .allowedOrigins("http://localhost:8080")
                    // 允许那些方法进行跨域访问
                    .allowedMethods("PUT", "DELETE", "GET", "POST", "OPTIONS", "HEAD")
                    // 允许哪些请求头进行跨域访问
                    .allowedHeaders("*")
                    // 是否支持用户凭证
                    .allowCredentials(false)
                    // 客户端缓存前一个响应时间
                    .maxAge(3600);
            }
        };
    }
}
