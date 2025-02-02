package com.hxls.system.convert;

import com.hxls.system.entity.SysDictTypeEntity;
import com.hxls.system.vo.SysDictTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysDictTypeConvert {
    SysDictTypeConvert INSTANCE = Mappers.getMapper(SysDictTypeConvert.class);

    SysDictTypeVO convert(SysDictTypeEntity entity);

    SysDictTypeEntity convert(SysDictTypeVO vo);

    List<SysDictTypeVO> convertList(List<SysDictTypeEntity> list);

}
