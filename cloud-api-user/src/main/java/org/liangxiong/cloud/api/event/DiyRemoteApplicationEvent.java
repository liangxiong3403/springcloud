package org.liangxiong.cloud.api.event;

import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 * @author liangxiong
 * @Date:2019-04-02
 * @Time:10:22
 * @Description 自定义远程应用事件
 */
public class DiyRemoteApplicationEvent extends RemoteApplicationEvent {

    private static final long serialVersionUID = 2950774536924746603L;

    /**
     * 无参构造器是必须地
     */
    protected DiyRemoteApplicationEvent() {
        super();
    }

    public DiyRemoteApplicationEvent(Object source, String originService, String destinationService) {
        super(source, originService, destinationService);
    }
}
