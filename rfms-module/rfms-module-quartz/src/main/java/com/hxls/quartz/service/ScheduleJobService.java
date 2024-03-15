package com.hxls.quartz.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.quartz.entity.ScheduleJobEntity;
import com.hxls.quartz.query.ScheduleJobQuery;
import com.hxls.quartz.vo.ScheduleJobVO;

import java.util.List;

/**
 * 定时任务
 *
 * @author
 *
 */
public interface ScheduleJobService extends BaseService<ScheduleJobEntity> {

    PageResult<ScheduleJobVO> page(ScheduleJobQuery query);

    void save(ScheduleJobVO vo);

    void update(ScheduleJobVO vo);

    void delete(List<Long> idList);

    void run(ScheduleJobVO vo);

    void changeStatus(ScheduleJobVO vo);
}
