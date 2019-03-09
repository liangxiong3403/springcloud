package org.liangxiong.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liangxiong
 * @Date:2019-03-09
 * @Time:11:07
 * @Description
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 用户名
     */
    private String username;

    /**
     * 年龄
     */
    private Integer age;

}
