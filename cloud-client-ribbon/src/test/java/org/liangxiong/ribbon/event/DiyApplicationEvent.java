package org.liangxiong.ribbon.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author liangxiong
 * @Date:2019-03-31
 * @Time:20:08
 * @Description 自定义事件
 */
public class DiyApplicationEvent extends ApplicationEvent {

    private static final long serialVersionUID = -8815371883373472297L;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DiyApplicationEvent(Object source) {
        super(source);
    }
}
