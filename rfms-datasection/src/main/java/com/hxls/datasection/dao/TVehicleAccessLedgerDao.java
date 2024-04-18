package com.hxls.datasection.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 车辆进出厂展示台账
*
* @author zhaohong 
* @since 1.0.0 2024-04-18
*/
@Mapper
public interface TVehicleAccessLedgerDao extends BaseDao<TVehicleAccessLedgerEntity> {
	
}