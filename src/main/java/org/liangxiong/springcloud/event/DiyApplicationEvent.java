package org.liangxiong.springcloud.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author liangxiong
 * @Date:2018-12-19
 * @Time:21:40
 * @Description 自定义事件, 继承@{link org.springframework.context.ApplicationEvent}
 */
public class DiyApplicationEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DiyApplicationEvent(Object source) {
        super(source);
    }

}
