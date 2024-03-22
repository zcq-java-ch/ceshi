package com.hxls.appointment.service;

import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;


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

    /**
     * 根据id获取详细表单信息
     * @param id id
     * @return
     */
    TAppointmentVO getDetailById(Long id);
}