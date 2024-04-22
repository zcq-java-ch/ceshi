package com.hxls.system.convert;

import com.hxls.system.entity.SysNoticeEntity;
import com.hxls.system.vo.SysNoticeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 系统消息表
*
* @author zhaohong 
* @since 1.0.0 2024-04-22
*/
@Mapper
public interface SysNoticeConvert {
    SysNoticeConvert INSTANCE = Mappers.getMapper(SysNoticeConvert.class);

    SysNoticeEntity convert(SysNoticeVO vo);

    SysNoticeVO convert(SysNoticeEntity entity);

    List<SysNoticeVO> convertList(List<SysNoticeEntity> list);

}
