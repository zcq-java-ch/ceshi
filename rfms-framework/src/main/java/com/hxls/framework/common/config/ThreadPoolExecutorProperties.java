package com.hxls.framework.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
* @Description : 自定义线程池参数
*/
@Component
@ConfigurationProperties(prefix = "cloud.thread")
@Data
public class ThreadPoolExecutorProperties {

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 非核心线程数的最大空闲时间，单位秒
     */
    private Integer keepAliveTime;

    /**
     * 阻塞队列大小
     */
    private Integer capacity;
}
