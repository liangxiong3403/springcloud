package org.liangxiong.ribbon.component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @author liangxiong
 * @Date:2019-03-17
 * @Time:14:43
 * @Description 通过 {@link java.util.concurrent.Future} 实现CircuitBreaker
 */
@Slf4j
public class FutureCircuitBreaker {

    private static final Random RANDOM = new Random();

    public interface Command<T> {

        /**
         * 正常执行操作
         *
         * @return
         * @throws Exception 抛出异常
         */
        T run() throws Exception;

        /**
         * 当熔断发生时,调用此方法
         *
         * @return
         */
        T fallback();
    }

    public static class RandomCommand implements Command<String> {

        @Override
        public String run() throws Exception {
            // 获取随机时间
            int time = RANDOM.nextInt(500);
            log.info("method execution time: {}", time);
            // 方法休眠
            TimeUnit.MILLISECONDS.sleep(time);
            return "method execution succeed!";
        }

        @Override
        public String fallback() {
            return "method timeout!";
        }
    }

    public static void main(String[] args) {
        // 获取线程池
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("future-pool-%d").build();
        ExecutorService executorService = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        // 构造任务
        Command<String> randomCommand = new RandomCommand();
        // 提交任务
        Future<String> future = executorService.submit(randomCommand::run);
        // 获取执行结果(等待300毫秒)
        String result;
        try {
            result = future.get(300, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("future execution error");
            // 调用超时回调方法
            result = randomCommand.fallback();
        }
        log.info("method execution result: {}", result);
        // 关闭线程池
        executorService.shutdown();
    }

}
