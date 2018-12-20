package org.liangxiong.springcloud.event;

import org.junit.Test;
import org.liangxiong.springcloud.listener.DiyApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author liangxiong
 * @Date:2018-12-19
 * @Time:21:48
 * @Description 测试自定义应用事件的发布和监听
 */
public class DiyApplicationEventTest {

    @Test
    public void testApplicationEvent() {
        // 注册自定义应用监听器方式一(自动调用refresh方法)
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(DiyApplicationListener.class);
        // 注册自定义应用监听器方式二
        ///DiyApplicationListener applicationListener = new DiyApplicationListener();
        ///applicationContext.addApplicationListener(applicationListener);
        // 注册自定义应用监听器方式三(借助于@Component)
        ///applicationContext.register(DiyApplicationListener.class);
        // 实例化自定义应用事件
        DiyApplicationEvent diyApplicationEvent = new DiyApplicationEvent("hello,kitty");
        DiyApplicationEvent diyApplicationEvent2 = new DiyApplicationEvent(666);
        DiyApplicationEvent diyApplicationEvent3 = new DiyApplicationEvent(3.14);
        // 启动上下文
        // applicationContext.refresh();
        // 发布事件(上下文启动以后才能发布事件)
        applicationContext.publishEvent(diyApplicationEvent);
        applicationContext.publishEvent(diyApplicationEvent2);
        applicationContext.publishEvent(diyApplicationEvent3);
    }
}
