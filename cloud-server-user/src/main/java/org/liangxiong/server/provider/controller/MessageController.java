package org.liangxiong.server.provider.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.liangxiong.cloud.api.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liangxiong
 * @Date:2019-03-22
 * @Time:13:37
 * @Description 消息处理
 */
@Slf4j
@RequestMapping("/kafka")
@RestController
public class MessageController {

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public MessageController(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送消息
     *
     * @param topic 消息主题
     * @param key   关键字
     * @param user  消息内容
     * @return
     */
    @PostMapping("/message/object")
    public JSONObject sendMessage(@RequestParam String topic, @RequestParam String key, @RequestBody User user) {
        ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(topic, key, user);
        JSONObject result = new JSONObject(4);
        try {
            SendResult<String, Object> sendResult = listenableFuture.get(3, TimeUnit.SECONDS);
            result.put("partition", sendResult.getRecordMetadata().partition());
            result.put("timestamp", sendResult.getRecordMetadata().timestamp());
        } catch (InterruptedException e) {
            log.error("线程被中断");
        } catch (ExecutionException e) {
            log.error("任务执行异常");
        } catch (TimeoutException e) {
            log.error("任务执行超时");
        }
        return result;
    }
}
