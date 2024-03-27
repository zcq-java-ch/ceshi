package com.hxls.appointment.service.impl;

import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.service.TAppointmentVehicleService;
import com.hxls.appointment.dao.TAppointmentVehicleMapper;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【t_appointment_vehicle(预约车辆信息表)】的数据库操作Service实现
* @createDate 2024-03-21 17:07:39
*/
@Service
@AllArgsConstructor
public class TAppointmentVehicleServiceImpl extends BaseServiceImpl<TAppointmentVehicleMapper, TAppointmentVehicle>
    implements TAppointmentVehicleService{

}




