package com.hxls.system.convert;

import com.hxls.system.entity.TBannerEntity;
import com.hxls.system.vo.TBannerVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* banner管理
*
* @author zhaohong 
* @since 1.0.0 2024-03-13
*/
@Mapper
public interface TBannerConvert {
    TBannerConvert INSTANCE = Mappers.getMapper(TBannerConvert.class);

    TBannerEntity convert(TBannerVO vo);

    TBannerVO convert(TBannerEntity entity);

    List<TBannerVO> convertList(List<TBannerEntity> list);

}