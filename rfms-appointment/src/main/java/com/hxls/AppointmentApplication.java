package com.hxls;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 新增模块演示
 *
 * @author
 *
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class AppointmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppointmentApplication.class, args);
	}

}
