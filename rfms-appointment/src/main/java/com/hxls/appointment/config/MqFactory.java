package com.hxls.appointment.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqFactory {

    @Bean
    public ConnectionFactory connectionFactory(RabbitMqProperties rabbitMqProperties) {

        System.out.println("此时的参数为:"+rabbitMqProperties);
        //创建工厂类
        CachingConnectionFactory cachingConnectionFactory=new CachingConnectionFactory();
        //用户名
        cachingConnectionFactory.setUsername(rabbitMqProperties.getUsername());
        //密码
        cachingConnectionFactory.setPassword(rabbitMqProperties.getPassword());
        //rabbitMQ地址
        cachingConnectionFactory.setHost(rabbitMqProperties.getHost());
        //rabbitMQ端口
        cachingConnectionFactory.setPort(Integer.parseInt(rabbitMqProperties.getPort()));
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        return  cachingConnectionFactory;
    }

    /**
     * 将配置好的信息放入
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return  factory;
    }

    /**
     * 创建一个RabbitAdmin
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}

