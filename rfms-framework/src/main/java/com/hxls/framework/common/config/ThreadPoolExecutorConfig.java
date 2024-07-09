package com.hxls.framework.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
* @Description : 自定义线程池配置
*/
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor executor(ThreadPoolExecutorProperties threadPoolExecutorProperties) {

        System.out.println("开始加载线程池"+ threadPoolExecutorProperties);

        return new ThreadPoolExecutor(
                threadPoolExecutorProperties.getCorePoolSize(),
                threadPoolExecutorProperties.getMaximumPoolSize(),
                threadPoolExecutorProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threadPoolExecutorProperties.getCapacity()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
