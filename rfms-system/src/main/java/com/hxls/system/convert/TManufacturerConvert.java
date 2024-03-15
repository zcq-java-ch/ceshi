package com.hxls.system.convert;

import com.hxls.system.entity.TManufacturerEntity;
import com.hxls.system.vo.TManufacturerVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 厂家管理表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TManufacturerConvert {
    TManufacturerConvert INSTANCE = Mappers.getMapper(TManufacturerConvert.class);

    TManufacturerEntity convert(TManufacturerVO vo);

    TManufacturerVO convert(TManufacturerEntity entity);

    List<TManufacturerVO> convertList(List<TManufacturerEntity> list);

}