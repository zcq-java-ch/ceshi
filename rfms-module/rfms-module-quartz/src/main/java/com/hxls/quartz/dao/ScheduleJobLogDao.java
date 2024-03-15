package com.hxls.quartz.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.quartz.entity.ScheduleJobLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 定时任务日志
*
* @author
*/
@Mapper
public interface ScheduleJobLogDao extends BaseDao<ScheduleJobLogEntity> {

}
