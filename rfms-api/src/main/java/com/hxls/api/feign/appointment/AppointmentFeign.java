package com.hxls.api.feign.appointment;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.ServerNames;
import com.hxls.api.vo.PageResult;
import com.hxls.api.vo.TAppointmentVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@FeignClient(name = ServerNames.APPOINTMENT_SERVER_NAME)
public interface AppointmentFeign {


    /**
     * 创建交换机和队列
     */
    @PostMapping(value = "api/appointment/establish")
    AppointmentDTO establish(@RequestBody AppointmentDTO data) throws Exception;

    /**
     * 用户客户端到平台的 建立站点队列
     * */
    @PostMapping("api/appointment/establishAgentToCloud")
    AppointmentDTO establishAgentToCloud(@RequestBody AppointmentDTO data);

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


    /**
     * 下发人员
     * @param data  人员信息
     * @return 是否成功
     */
    @PostMapping(value = "api/appointment/issuedPeople")
    Boolean issuedPeople(@RequestBody cn.hutool.json.JSONObject data );


    /**
     * 下发车辆
     * @param data  车辆
     * @return 是否成功
     */
    @PostMapping(value = "api/appointment/issuedVehicle")
    Boolean issuedVehicle(@RequestBody JSONObject data );


    @GetMapping(value = "api/appointment/{id}")
    JSONObject guardInformation(@PathVariable("id") Long id);


    @GetMapping(value = "api/appointment/sum/{id}/{type}")
    JSONObject appointmentSum(@PathVariable("id") Long id, @PathVariable ("type")  Long type);

    @PostMapping("api/appointment/queryTheNumberOfResidencies")
    JSONObject queryTheNumberOfResidencies(@RequestParam Long siteId);

    @PostMapping("api/appointment/checkTheDetailsOfExternalAppointments")
    JSONArray checkTheDetailsOfExternalAppointments(@RequestParam Long siteId, @RequestParam Integer page, @RequestParam Integer limit);

    @PostMapping("api/appointment/queryStatisticsallPeopleReservation")
    JSONObject queryStatisticsallPeopleReservation();
    @GetMapping("api/appointment/queryTotalAppointments")
    public com.alibaba.fastjson.JSONObject queryTotalAppointments(@RequestParam Long siteId);

    @GetMapping("api/appointment/queryappointmentFormspecifyLicensePlatesAndEntourage")
    public com.alibaba.fastjson.JSONObject queryappointmentFormspecifyLicensePlatesAndEntourage(@RequestParam String plateNumber, @RequestParam String recordTime);
}
