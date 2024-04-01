package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 区域通道随机码与设备中间表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Mapper
public interface SysAreacodeDeviceDao extends BaseDao<SysAreacodeDeviceEntity> {
	
}