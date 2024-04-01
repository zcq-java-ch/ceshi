package com.hxls.system.convert;

import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.vo.SysAreacodeDeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 区域通道随机码与设备中间表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Mapper
public interface SysAreacodeDeviceConvert {
    SysAreacodeDeviceConvert INSTANCE = Mappers.getMapper(SysAreacodeDeviceConvert.class);

    SysAreacodeDeviceEntity convert(SysAreacodeDeviceVO vo);

    SysAreacodeDeviceVO convert(SysAreacodeDeviceEntity entity);

    List<SysAreacodeDeviceVO> convertList(List<SysAreacodeDeviceEntity> list);

}