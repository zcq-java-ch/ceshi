package com.hxls.appointment.server;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RabbitMqManager {


    private final RabbitAdmin rabbitAdmin;

    public void declareExchangeAndQueue(String exchangeName, String queueName) {
        // 声明交换机
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        rabbitAdmin.declareExchange(exchange);

        // 声明队列
        Queue queue = new Queue(queueName, true, false, false);
        rabbitAdmin.declareQueue(queue);

        // 绑定队列到交换机
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName);
        rabbitAdmin.declareBinding(binding);
    }
}
