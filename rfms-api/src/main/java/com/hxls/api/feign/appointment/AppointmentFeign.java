package com.hxls.api.feign.appointment;


import cn.hutool.json.JSONObject;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.ServerNames;
import com.hxls.api.vo.PageResult;
import com.hxls.api.vo.TAppointmentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
    PageResult<TAppointmentVO> board(@RequestBody AppointmentDTO data);

    /**
     * 逻辑删除预约看板数据
     * @param id id值
     */
    @GetMapping(value = "api/appointment/del")
    void delAppointment(@RequestParam  Long id);

    @GetMapping(value = "api/appointment/{id})")
    JSONObject guardInformation(@PathVariable("id") Long id);
}
