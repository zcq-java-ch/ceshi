package com.hxls.quartz.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.quartz.entity.ScheduleJobLogEntity;
import com.hxls.quartz.query.ScheduleJobLogQuery;
import com.hxls.quartz.vo.ScheduleJobLogVO;

/**
 * 定时任务日志
 *
 * @author
 *
 */
public interface ScheduleJobLogService extends BaseService<ScheduleJobLogEntity> {

    PageResult<ScheduleJobLogVO> page(ScheduleJobLogQuery query);

}
