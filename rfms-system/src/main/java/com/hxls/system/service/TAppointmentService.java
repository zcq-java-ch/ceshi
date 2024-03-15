package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.TAppointmentVO;
import com.hxls.system.query.TAppointmentQuery;
import com.hxls.system.entity.TAppointmentEntity;

import java.util.List;

/**
 * 预约信息表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
public interface TAppointmentService extends BaseService<TAppointmentEntity> {

    PageResult<TAppointmentVO> page(TAppointmentQuery query);

    void save(TAppointmentVO vo);

    void update(TAppointmentVO vo);

    void delete(List<Long> idList);
}