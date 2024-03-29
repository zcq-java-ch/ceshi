package com.hxls.datasection.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 车辆出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Mapper
public interface TVehicleAccessRecordsDao extends BaseDao<TVehicleAccessRecordsEntity> {
	
}