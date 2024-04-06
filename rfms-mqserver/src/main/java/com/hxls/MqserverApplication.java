package com.hxls;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MqserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqserverApplication.class, args);
        System.out.println("Hello world!");
    }
}