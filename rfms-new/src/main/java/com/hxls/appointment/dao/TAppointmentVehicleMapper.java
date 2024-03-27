package com.hxls.appointment.dao;

import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.framework.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【t_appointment_vehicle(预约车辆信息表)】的数据库操作Mapper
* @createDate 2024-03-21 17:07:39
* @Entity com.hxls.appointment.pojo.entity.TAppointmentVehicle
*/
@Mapper
public interface TAppointmentVehicleMapper extends BaseDao<TAppointmentVehicle> {

}




