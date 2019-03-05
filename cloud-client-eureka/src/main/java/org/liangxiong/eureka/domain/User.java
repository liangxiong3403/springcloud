package org.liangxiong.eureka.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:16:29
 * @Description
 */
@Setter
@Getter
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
