package org.liangxiong.server.provider.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:11:19
 * @Description 用于broker的对象序列化
 */
@Slf4j
public class ObjectSerializer implements Serializer<Object> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    /**
     * @param topic 消息主题
     * @param data  消息内容
     * @return
     */
    @Override
    public byte[] serialize(String topic, Object data) {
        byte[] result = new byte[0];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(data);
            // 获取序列化以后的字节数组
            result = bos.toByteArray();
        } catch (Exception e) {
            log.error("Serializer execution error: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public void close() {

    }
}
