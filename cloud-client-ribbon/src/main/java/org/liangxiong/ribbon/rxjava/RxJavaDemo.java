package org.liangxiong.ribbon.rxjava;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author liangxiong
 * @Date:2019-03-17
 * @Time:15:42
 * @Description 基于Rx1.x
 */
@Slf4j
public class RxJavaDemo {

    public static void main(String[] args) throws Exception {
        standardReactiveMode();
    }

    /**
     * 单个元素
     */
    private static void singleElement() {
        Single.just("hello,world").subscribeOn(Schedulers.io()).subscribe(RxJavaDemo::println);
    }

    /**
     * 多个元素
     */
    private static void multipleElements() throws InterruptedException {
        Observable.from(Arrays.asList("China", "American", "Australia", "Japan"))
                .subscribeOn(Schedulers.computation())
                .subscribe(RxJavaDemo::println);
        // 等待执行结果
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * 标准reactive模式
     */
    private static void standardReactiveMode() throws InterruptedException {
        int number = 6;
        Observable.from(Arrays.asList("English", "Chinese", "Spanish", "French", "Russian"))
            .subscribeOn(Schedulers.newThread())
            .subscribe(value -> {
                if (value.length() > number) {
                    println(value);
                } else {
                    throw new IllegalArgumentException("character length must greater than 6!");
                }
            }
            ,
            e -> log.error("method execution failed: {}", e.getMessage()), () -> log.error("method execution finish"));
        // 等待执行结果
        TimeUnit.MILLISECONDS.sleep(100);
    }


    private static void println(String str) {
        System.out.println(Thread.currentThread().getName() + " : " + str);
    }
}
