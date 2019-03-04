package org.liangxiong.springcloud.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liangxiong
 * @Date:2019-03-04
 * @Time:11:14
 * @Description 用户
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "user")
public class User {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 年龄
     */
    private Integer age;
}
