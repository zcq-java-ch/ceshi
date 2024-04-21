package com.hxls.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rabbit")
@Data
public class RabbitMqProperties {

    /**
     * ip
     */
    private String ip;

    /**
     * post
     */
    private String post;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String psw;
}
