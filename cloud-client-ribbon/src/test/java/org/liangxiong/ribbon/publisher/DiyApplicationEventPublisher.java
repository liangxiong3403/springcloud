package org.liangxiong.ribbon.publisher;

import org.liangxiong.ribbon.config.EventAutoConfiguration;
import org.liangxiong.ribbon.event.DiyApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author liangxiong
 * @Date:2019-03-31
 * @Time:20:11
 * @Description
 */
public class DiyApplicationEventPublisher {

    public static void main(String[] args) {
        // 声明上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // 注册事件
        context.register(EventAutoConfiguration.class);
        // 刷新上下文
        context.refresh();
        // 声明事件
        DiyApplicationEvent event = new DiyApplicationEvent("diy my event");
        // 发布事件
        context.publishEvent(event);

    }
}
