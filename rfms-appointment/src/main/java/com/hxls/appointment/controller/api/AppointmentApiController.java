package com.hxls.appointment.controller.api;

import com.hxls.api.dto.AppointmentDTO;
import com.hxls.appointment.server.MqServer;
import com.hxls.appointment.server.RabbitMqManager;
import com.hxls.framework.common.cache.RedisCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/appointment")
@Tag(name = "api接口")
@AllArgsConstructor
public class AppointmentApiController {

    private final RabbitMqManager rabbitMqManager;
    private final RedisCache redisCache;

    @PostMapping("establish")
    @Operation(summary = "建立站点队列")
    public AppointmentDTO establish(@RequestBody AppointmentDTO data){
        System.out.println("接收到信息");
        System.out.println(data);
        if ( redisCache.get(data.getIp()) ==null){
            rabbitMqManager.declareExchangeAndQueue(data.getExchangeName(), data.getQueueName());
            data.setResult(true);
        }

        return data;
    }



}
