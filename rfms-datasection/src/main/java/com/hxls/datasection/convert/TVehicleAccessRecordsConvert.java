package com.hxls.datasection.convert;

import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 车辆出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Mapper
public interface TVehicleAccessRecordsConvert {
    TVehicleAccessRecordsConvert INSTANCE = Mappers.getMapper(TVehicleAccessRecordsConvert.class);

    TVehicleAccessRecordsEntity convert(TVehicleAccessRecordsVO vo);

    TVehicleAccessRecordsVO convert(TVehicleAccessRecordsEntity entity);

    List<TVehicleAccessRecordsVO> convertList(List<TVehicleAccessRecordsEntity> list);

}