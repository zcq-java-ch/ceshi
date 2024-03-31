package com.hxls.api.feign.appointment;

import com.hxls.api.dto.AppointmentDTO;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = ServerNames.APPOINTMENT_SERVER_NAME)
public interface AppointmentFeign {


    /**
     * 创建交换机和队列
     */
    @PostMapping(value = "api/appointment/establish")
    AppointmentDTO establish(@RequestBody AppointmentDTO data) throws Exception;


}
