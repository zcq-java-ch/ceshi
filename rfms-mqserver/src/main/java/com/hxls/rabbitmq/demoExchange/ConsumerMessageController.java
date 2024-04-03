package com.hxls.rabbitmq.demoExchange;

import com.alibaba.fastjson.JSONObject;
import com.hxls.rabbitmq.domain.MessageSendDto;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumerMessageController {

    /**
     * Mryang
     * 监听站点返回 消息
     * */
    @RabbitHandler
    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicQueueNameFromCloud()}")
    public void setFaceIpNormalprocess(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
        MessageProperties properties = message.getMessageProperties();
//        long tag = properties.getDeliveryTag();
//        log.info("简单模式的消费者收到:{}",s);
        System.out.println("简单模式的消费者收到:"+s);
        MessageSendDto student = JSONObject.parseObject(s, MessageSendDto.class);
//        log.info(student.toString());
        System.out.println("student.toString()"+student.toString());
        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }
}
