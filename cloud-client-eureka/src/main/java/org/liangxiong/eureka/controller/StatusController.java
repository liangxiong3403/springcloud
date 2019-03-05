package org.liangxiong.eureka.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author liangxiong
 * @Date:2019-03-05
 * @Time:14:34
 * @Description
 */
@RestController
public class StatusController {

    @RequestMapping("/status")
    public String getStatus() {
        return "application starting success";
    }
}
