package com.hxls.appointment.dao;

import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.framework.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【t_appointment_personnel(预约人员信息表)】的数据库操作Mapper
* @createDate 2024-03-21 16:57:24
* @Entity com/hxls/appointment.pojo/entity.TAppointmentPersonnel
*/
@Mapper
public interface TAppointmentPersonnelDao extends BaseDao<TAppointmentPersonnel> {

}




