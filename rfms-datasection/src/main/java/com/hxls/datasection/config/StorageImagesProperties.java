package com.hxls.datasection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 存储配置项
 *
 * @author
 *
 */
@Data
@ConfigurationProperties(prefix = "storageImage")
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
