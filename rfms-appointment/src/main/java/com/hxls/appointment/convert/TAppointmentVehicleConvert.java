package com.hxls.appointment.convert;

import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TAppointmentVehicleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 预约信息表
*
* @author zhuchuanqiu
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TAppointmentVehicleConvert {
    TAppointmentVehicleConvert INSTANCE = Mappers.getMapper(TAppointmentVehicleConvert.class);

    TAppointmentVehicle convert(TAppointmentVehicleVO vo);

    TAppointmentVehicleVO convert(TAppointmentVehicle entity);

    List<TAppointmentVehicleVO> convertList(List<TAppointmentVehicle> list);

}