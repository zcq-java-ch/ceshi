package com.hxls.system.convert;

import com.hxls.system.entity.TAppointmentEntity;
import com.hxls.system.vo.TAppointmentVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 预约信息表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TAppointmentConvert {
    TAppointmentConvert INSTANCE = Mappers.getMapper(TAppointmentConvert.class);

    TAppointmentEntity convert(TAppointmentVO vo);

    TAppointmentVO convert(TAppointmentEntity entity);

    List<TAppointmentVO> convertList(List<TAppointmentEntity> list);

}