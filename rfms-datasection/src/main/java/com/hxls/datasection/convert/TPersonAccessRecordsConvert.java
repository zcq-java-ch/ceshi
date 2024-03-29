package com.hxls.datasection.convert;

import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
* 人员出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Mapper
public interface TPersonAccessRecordsConvert {
    TPersonAccessRecordsConvert INSTANCE = Mappers.getMapper(TPersonAccessRecordsConvert.class);

    TPersonAccessRecordsEntity convert(TPersonAccessRecordsVO vo);

    TPersonAccessRecordsVO convert(TPersonAccessRecordsEntity entity);

    List<TPersonAccessRecordsVO> convertList(List<TPersonAccessRecordsEntity> list);

}