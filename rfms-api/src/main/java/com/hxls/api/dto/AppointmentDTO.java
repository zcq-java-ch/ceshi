package com.hxls.api.dto;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;

@Data
@Tag(name="创建消息队列")
public class AppointmentDTO implements Serializable {

    private String exchangeName ;

    private String queueName;


}
