package com.hxls;

import com.hxls.datasection.config.StorageImagesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(StorageImagesProperties.class)
public class DatasectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatasectionApplication.class, args);
        System.out.println("Hello world!");
    }
}