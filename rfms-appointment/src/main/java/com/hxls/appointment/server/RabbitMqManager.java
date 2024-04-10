package com.hxls.appointment.server;

import com.hxls.api.dto.appointment.AppointmentDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class RabbitMqManager {

    private final RabbitAdmin rabbitAdmin;

    public void declareExchangeAndQueue(AppointmentDTO appointmentDTO) {
        // 声明交换机
        DirectExchange exchange = new DirectExchange(appointmentDTO.getExchangeName(), true, false);
        rabbitAdmin.declareExchange(exchange);

        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 60000); // 60 seconds //60s删除
        // 声明队列
        Queue queue1 = new Queue(appointmentDTO.getCarToAgentQueueName(), true, false, false,args);
        Queue queue2 = new Queue(appointmentDTO.getCarToCloudQueueName(), true, false, false,args);
        Queue queue3 = new Queue(appointmentDTO.getFaceToAgentQueueName(), true, false, false,args);
        Queue queue4 = new Queue(appointmentDTO.getFaceToCloudQueueName(), true, false, false,args);
        rabbitAdmin.declareQueue(queue1);
        rabbitAdmin.declareQueue(queue2);
        rabbitAdmin.declareQueue(queue3);
        rabbitAdmin.declareQueue(queue4);

        // 绑定队列到交换机
        Binding binding1 = BindingBuilder.bind(queue1).to(exchange).with(appointmentDTO.getCarToAgentroutingKey());
        Binding binding2 = BindingBuilder.bind(queue2).to(exchange).with(appointmentDTO.getCarToCloudroutingKey());
        Binding binding3 = BindingBuilder.bind(queue3).to(exchange).with(appointmentDTO.getFaceToAgentroutingKey());
        Binding binding4 = BindingBuilder.bind(queue4).to(exchange).with(appointmentDTO.getFaceToCloudroutingKey());
        rabbitAdmin.declareBinding(binding1);
        rabbitAdmin.declareBinding(binding2);
        rabbitAdmin.declareBinding(binding3);
        rabbitAdmin.declareBinding(binding4);
    }

    public void declareExchangeAndQueueToCloud(AppointmentDTO appointmentDTO) {
        // 声明交换机
        DirectExchange exchange = new DirectExchange(appointmentDTO.getExchangeName(), true, false);
        rabbitAdmin.declareExchange(exchange);

        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 60000); // 60 seconds //60s删除
        // 声明队列
        Queue queue2 = new Queue(appointmentDTO.getCarToCloudQueueName(), true, false, false,args);
        Queue queue4 = new Queue(appointmentDTO.getFaceToCloudQueueName(), true, false, false,args);
        rabbitAdmin.declareQueue(queue2);
        rabbitAdmin.declareQueue(queue4);

        // 绑定队列到交换机
        Binding binding2 = BindingBuilder.bind(queue2).to(exchange).with(appointmentDTO.getCarToCloudroutingKey());
        Binding binding4 = BindingBuilder.bind(queue4).to(exchange).with(appointmentDTO.getFaceToCloudroutingKey());
        rabbitAdmin.declareBinding(binding2);
        rabbitAdmin.declareBinding(binding4);
    }
}
