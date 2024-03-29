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

    /**
     * 分页查询
     * @param query
     * @return
     */
    PageResult<TAppointmentVO> page(TAppointmentQuery query);

    /**
     * 保存预约信息
     * @param vo
     */
    void save(TAppointmentVO vo);

    /**
     * 修改本次预约信息
     * @param vo
     */
    void update(TAppointmentVO vo);

    /**
     * 删除预约表单
     * @param idList
     */
    void delete(List<Long> idList);

    /**
     * 根据id获取详细表单信息
     * @param id id
     * @return
     */
    TAppointmentVO getDetailById(Long id);

    /**
     * 查看权限下面的列表
     * @param query
     * @return
     */
    PageResult<TAppointmentVO> pageByAuthority(TAppointmentQuery query);

    /**
     * 修改审核状态
     * @param vo
     */
    void updateByAudit(TAppointmentVO vo);
}