package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.TManufacturerEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 厂家管理表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TManufacturerDao extends BaseDao<TManufacturerEntity> {
	
}