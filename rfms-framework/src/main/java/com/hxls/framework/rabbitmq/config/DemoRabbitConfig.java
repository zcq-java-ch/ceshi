package com.hxls.framework.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class DemoRabbitConfig {

    // 交换机名字
    public final static String SITE_EXCHANGE = "XICHANG_EXCHANGE";

    // 队列名称 【站点-类型-主机ip】 [平台到客户端的通道]
    public final static String SITE_FACE_TOAGENT = "XICHANG_FACE_192.102.0.76_TOAGENT";
    public final static String SITE_CAR_TOAGENT = "XICHANG_CAR_20.20.20.32_TOAGENT";

    // 队列名称 【站点-类型-主机ip】 [客户端到平台的通道]
    public final static String SITE_FACE_TOCLOUD = "XICHANG_FACE_192.102.0.76_TOCLOUD";
    public final static String SITE_CAR_TOCLOUD = "XICHANG_CAR_20.20.20.32_TOCLOUD";


    // 路由键 站点——类型 【平台到站点用的路由】
    public final static String SITE_ROUTING_FACE_TOAGENT = "XICHANG_ROUTING_FACE_TOAGENT";
    public final static String SITE_ROUTING_CAR_TOAGENT = "XICHANG_ROUTING_CAR_TOAGENT";

    // 路由键 站点——类型 【站点到平台用的路由】
    public final static String SITE_ROUTING_FACE_TOCLOUD = "XICHANG_ROUTING_FACE_TOCLOUD";
    public final static String SITE_ROUTING_CAR_TOCLOUD = "XICHANG_ROUTING_CAR_TOCLOUD";


    //声明交换机
    @Bean
    TopicExchange stationExchange() {
        return new TopicExchange(SITE_EXCHANGE);
    }

    //声明一个人脸队列
    @Bean
    public Queue faceQueueToAgent() {
        return new Queue(DemoRabbitConfig.SITE_FACE_TOAGENT);
    }
    //声明一个车辆队列
    @Bean
    public Queue carQueueToAgent() {
        return new Queue(DemoRabbitConfig.SITE_CAR_TOAGENT);
    }

    @Bean
    public Queue faceQueueToCloud() {
        return new Queue(DemoRabbitConfig.SITE_FACE_TOCLOUD);
    }
    //声明一个车辆队列
    @Bean
    public Queue carQueueToCloud() {
        return new Queue(DemoRabbitConfig.SITE_CAR_TOCLOUD);
    }

    //将firstQueue和topicExchange绑定,而且绑定的键值为topic.man
    //这样只要是消息携带的路由键是topic.man,才会分发到该队列
    @Bean
    Binding bindingExchangeMessageFaceToAgent() {
        return BindingBuilder
                .bind(faceQueueToAgent())
                .to(stationExchange())
                .with(SITE_ROUTING_FACE_TOAGENT);
    }

    //将secondQueue和topicExchange绑定,而且绑定的键值为用上通配路由键规则topic.#
    // 这样只要是消息携带的路由键是以topic.开头,都会分发到该队列
    @Bean
    Binding bindingExchangeMessageCarToAgent() {
        return BindingBuilder
                .bind(carQueueToAgent())
                .to(stationExchange())
                .with(SITE_ROUTING_CAR_TOAGENT);
    }

    @Bean
    Binding bindingExchangeMessageFaceToCloud() {
        return BindingBuilder
                .bind(faceQueueToCloud())
                .to(stationExchange())
                .with(SITE_ROUTING_FACE_TOCLOUD);
    }

    @Bean
    Binding bindingExchangeMessageCaToCloudr() {
        return BindingBuilder
                .bind(carQueueToCloud())
                .to(stationExchange())
                .with(SITE_ROUTING_CAR_TOCLOUD);
    }

    // 死信交换机
//    public final static String SITE_DEAD_EXCHANGE = "XICHANG_DEAD_EXCHANGE";
//
//    // 死信队列 【站点-类型-主机ip】
//    public final static String SITE_DEAD_FACE = "XICHANG_DEAD_FACE_20.20.20.32";
//    public final static String SITE_DEAD_CAR = "XICHANG_DEAD_CAR_20.20.20.32";
//    // 死信路由键 站点——类型
//    public final static String SITE_DEAD_ROUTING_FACE = "XICHANG_DEAD_ROUTING_FACE";
//    public final static String SITE_DEAD_ROUTING_CAR = "XICHANG_DEAD_ROUTING_CAR";
//    // 声明死信队列
//    @Bean
//    public Queue deadLetterFaceQueue() {
//        return QueueBuilder.durable(SITE_DEAD_FACE)
//                .withArgument("x-dead-letter-exchange", SITE_DEAD_EXCHANGE)
//                .withArgument("x-dead-letter-routing-key", SITE_DEAD_ROUTING_FACE)
//                .ttl(10000)
//                .build();
//    }
//    @Bean
//    public Queue deadLetterCarQueue() {
//        return QueueBuilder.durable(SITE_DEAD_CAR)
//                .ttl(10000)
//                .withArgument("x-dead-letter-exchange", SITE_DEAD_EXCHANGE)
//                .withArgument("x-dead-letter-routing-key", SITE_DEAD_ROUTING_CAR)
//                .ttl(10000)
//                .build();
//    }
//
//    // 生明死信交换机
//    @Bean
//    public DirectExchange deadLetterExchange() {
//        return new DirectExchange(SITE_DEAD_EXCHANGE);
//    }
//
//    // 死信交换机与队列进行绑定
//    @Bean
//    public Binding deadLetterFaceBinding() {
//        return BindingBuilder.bind(deadLetterFaceQueue()).to(deadLetterExchange()).with(SITE_DEAD_ROUTING_FACE);
//    }
//
//    @Bean
//    public Binding deadLetterCarBinding() {
//        return BindingBuilder.bind(deadLetterCarQueue()).to(deadLetterExchange()).with(SITE_DEAD_ROUTING_CAR);
//    }

    @Bean
    public MessageConverter jsonToMapMessageConverter() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        defaultClassMapper.setTrustedPackages("com.hxls.framework.rabbitmq.domain"); // trusted packages
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
        return jackson2JsonMessageConverter;
    }


}
