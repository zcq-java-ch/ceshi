package com.hxls.system.convert;

import com.hxls.system.entity.SysUserTokenEntity;
import com.hxls.system.vo.SysUserTokenVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户Token
 *
 * @author
 */
@Mapper
public interface SysUserTokenConvert {
    SysUserTokenConvert INSTANCE = Mappers.getMapper(SysUserTokenConvert.class);

    SysUserTokenEntity convert(SysUserTokenVO vo);

    SysUserTokenVO convert(SysUserTokenEntity entity);

}
