package com.hxls.system.convert;

import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.vo.TVehicleExcelVO;
import com.hxls.system.vo.TVehicleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 通用车辆管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TVehicleConvert {
    TVehicleConvert INSTANCE = Mappers.getMapper(TVehicleConvert.class);

    TVehicleEntity convert(TVehicleVO vo);

    TVehicleVO convert(TVehicleEntity entity);


    List<TVehicleEntity> convertToEntityList(List<TVehicleVO> vo);

    List<TVehicleVO> convertList(List<TVehicleEntity> list);

    List<TVehicleEntity> convertListEntity(List<TVehicleExcelVO> list);

}
