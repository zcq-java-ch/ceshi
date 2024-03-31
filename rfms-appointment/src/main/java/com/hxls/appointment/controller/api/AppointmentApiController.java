package com.hxls.appointment.controller.api;

import com.hxls.api.dto.AppointmentDTO;
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


    @PostMapping("establish")
    @Operation(summary = "访问")
    public AppointmentDTO establish(@RequestBody AppointmentDTO data){
        System.out.println("接收到信息");
        System.out.println(data);
        return data;
    }



}
