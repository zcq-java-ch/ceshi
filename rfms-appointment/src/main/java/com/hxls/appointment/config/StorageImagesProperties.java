package com.hxls.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储配置项
 *
 * @author
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "storageimage")
public class StorageImagesProperties {
    /**
     * 通用配置项
     */
    private StorageConfig config;

    @Data
    public static class StorageConfig {
        /**
         * 访问域名
         */
        private String domain;
    }
}
