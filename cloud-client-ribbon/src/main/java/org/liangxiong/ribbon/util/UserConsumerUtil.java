package org.liangxiong.ribbon.util;

import org.liangxiong.cloud.api.domain.User;

/**
 * @author liangxiong
 * @Date:2019-03-29
 * @Time:11:00
 * @Description
 */
public class UserConsumerUtil {

    private UserConsumerUtil() {
    }

    /**
     * 根据数据类型返回消息内容
     *
     * @param source 原始消息内容
     * @return
     */
    public static User getUserFromPayload(Object source) {
        User user = new User();
        // 注意:需要判断消息类型
        if (source.getClass().isArray()) {
            // 消息为二进制
            user = UserDeserializeUtil.deserializeObject((byte[]) source);
        } else if (source instanceof User) {
            // 消息为原始对象(未被序列化)
            user = (User) source;
        }
        return user;
    }
}
