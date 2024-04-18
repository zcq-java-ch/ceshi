package com.hxls.datasection.convert;

import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 车辆进出厂展示台账
*
* @author zhaohong 
* @since 1.0.0 2024-04-18
*/
@Mapper
public interface TVehicleAccessLedgerConvert {
    TVehicleAccessLedgerConvert INSTANCE = Mappers.getMapper(TVehicleAccessLedgerConvert.class);

    TVehicleAccessLedgerEntity convert(TVehicleAccessLedgerVO vo);

    TVehicleAccessLedgerVO convert(TVehicleAccessLedgerEntity entity);

    List<TVehicleAccessLedgerVO> convertList(List<TVehicleAccessLedgerEntity> list);

}