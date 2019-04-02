package org.liangxiong.ribbon.controller;

import org.liangxiong.cloud.api.domain.User;
import org.liangxiong.cloud.api.event.DiyRemoteApplicationEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.*;

/**
 * @author liangxiong
 * @Date:2019-04-02
 * @Time:10:32
 * @Description 自定义事件发布入口
 */
@RestController
@RequestMapping("/diy/event")
public class UserEventController implements ApplicationContextAware, ApplicationEventPublisherAware {

    private ApplicationContext context;

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 发布自定义事件
     *
     * @param user 参数
     * @return
     */
    @PostMapping("/user")
    public boolean publishUserEvent(@RequestBody User user, @RequestParam(required = false, value = "destinationService") String destinationService) {
        // 获取应用上下文id
        String applicationId = context.getId();
        // 构建事件
        DiyRemoteApplicationEvent event = new DiyRemoteApplicationEvent(user, applicationId, destinationService);
        // 发布事件
        publisher.publishEvent(event);
        return true;
    }

}
