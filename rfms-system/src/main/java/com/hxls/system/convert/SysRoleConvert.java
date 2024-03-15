package com.hxls.system.convert;

import com.hxls.system.entity.SysRoleEntity;
import com.hxls.system.vo.SysRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysRoleConvert {
    SysRoleConvert INSTANCE = Mappers.getMapper(SysRoleConvert.class);

    SysRoleVO convert(SysRoleEntity entity);

    SysRoleEntity convert(SysRoleVO vo);

    List<SysRoleVO> convertList(List<SysRoleEntity> list);

}
