package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.TAppointmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 预约信息表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TAppointmentDao extends BaseDao<TAppointmentEntity> {
	
}