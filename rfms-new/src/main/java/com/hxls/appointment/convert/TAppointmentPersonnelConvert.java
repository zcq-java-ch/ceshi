package com.hxls.appointment.convert;

import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
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
public interface TAppointmentPersonnelConvert {
    TAppointmentPersonnelConvert INSTANCE = Mappers.getMapper(TAppointmentPersonnelConvert.class);

    TAppointmentPersonnel convert(TAppointmentPersonnelVO vo);

    TAppointmentPersonnelVO convert(TAppointmentPersonnel entity);

    List<TAppointmentPersonnelVO> convertList(List<TAppointmentPersonnel> list);


}