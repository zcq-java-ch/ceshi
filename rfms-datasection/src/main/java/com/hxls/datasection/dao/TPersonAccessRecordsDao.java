package com.hxls.datasection.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 人员出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Mapper
public interface TPersonAccessRecordsDao extends BaseDao<TPersonAccessRecordsEntity> {
	
}