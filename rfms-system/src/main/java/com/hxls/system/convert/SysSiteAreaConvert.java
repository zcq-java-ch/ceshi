package com.hxls.system.convert;

import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.vo.SysSiteAreaVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 站点区域表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Mapper
public interface SysSiteAreaConvert {
    SysSiteAreaConvert INSTANCE = Mappers.getMapper(SysSiteAreaConvert.class);

    SysSiteAreaEntity convert(SysSiteAreaVO vo);

    SysSiteAreaVO convert(SysSiteAreaEntity entity);

    List<SysSiteAreaVO> convertList(List<SysSiteAreaEntity> list);

}