package com.hxls.appointment.convert;

import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.vo.TAppointmentVehicleVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TRecordSupplementConvert {

    TRecordSupplementConvert INSTANCE = Mappers.getMapper(TRecordSupplementConvert.class);

    TSupplementRecord convert(TSupplementRecordVO vo);

    TSupplementRecordVO convert(TSupplementRecord entity);

    List<TSupplementRecordVO> convertList(List<TSupplementRecord> list);
}
