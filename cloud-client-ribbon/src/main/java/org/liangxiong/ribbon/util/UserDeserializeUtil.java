package org.liangxiong.ribbon.util;

import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * @author liangxiong
 * @Date:2019-03-23
 * @Time:18:28
 * @Description 反序列化工具类
 */
@Slf4j
public class UserDeserializeUtil {

    private UserDeserializeUtil() {
    }

    /**
     * 对字节数组进行反序列化
     *
     * @param source
     */
    public static User deserializeObject(byte[] source) {
        User user = new User();
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(source);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                Object message = ois.readObject();
                if (message instanceof User) {
                    user = (User) message;
                }
            }
        } catch (Exception e) {
            log.error("Deserialize execution error!");
        }
        return user;
    }
}
