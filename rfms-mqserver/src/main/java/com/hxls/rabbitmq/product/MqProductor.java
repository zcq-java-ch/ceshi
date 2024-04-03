package com.hxls.rabbitmq.product;

import com.alibaba.fastjson.JSON;
import com.hxls.rabbitmq.domain.MessageSendDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqProductor {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, MessageSendDto data){
        rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(data));
    }
}