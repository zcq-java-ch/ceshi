package com.hxls.api.feign.appointment;

import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.ServerNames;
import com.hxls.api.vo.TAppointmentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = ServerNames.APPOINTMENT_SERVER_NAME)
public interface AppointmentFeign {


    /**
     * 创建交换机和队列
     */
    @PostMapping(value = "api/appointment/establish")
    AppointmentDTO establish(@RequestBody AppointmentDTO data) throws Exception;

    /**
     * 获取安保信息
     */
    @PostMapping(value = "api/appointment/board")
    List<TAppointmentVO> board(@RequestBody AppointmentDTO data);

}
