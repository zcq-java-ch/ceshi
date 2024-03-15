package com.hxls.quartz.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.quartz.entity.ScheduleJobEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 定时任务
*
* @author
*/
@Mapper
public interface ScheduleJobDao extends BaseDao<ScheduleJobEntity> {

}
