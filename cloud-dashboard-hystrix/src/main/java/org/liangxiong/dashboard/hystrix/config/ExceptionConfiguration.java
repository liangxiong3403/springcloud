package org.liangxiong.dashboard.hystrix.config;

import org.liangxiong.dashboard.hystrix.controller.IndexController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;

/**
 * @author liangxiong
 * @Date:2019-03-12
 * @Time:10:34
 * @Description 异常处理配置类
 */
@RestControllerAdvice(assignableTypes = {IndexController.class})
public class ExceptionConfiguration {

    @ExceptionHandler(TimeoutException.class)
    public String handlerControllerException(TimeoutException e) {
        return e.getMessage();
    }

}
