package com.hxls.system.convert;

import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.vo.TDeviceManagementVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 设备管理表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TDeviceManagementConvert {
    TDeviceManagementConvert INSTANCE = Mappers.getMapper(TDeviceManagementConvert.class);

    TDeviceManagementEntity convert(TDeviceManagementVO vo);

    TDeviceManagementVO convert(TDeviceManagementEntity entity);

    List<TDeviceManagementVO> convertList(List<TDeviceManagementEntity> list);

}