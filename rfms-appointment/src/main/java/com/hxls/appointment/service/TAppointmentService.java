package com.hxls.appointment.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TAppointmentVehicleVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import org.springframework.web.multipart.MultipartFile;


import java.util.Date;
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

    /**
     * 根据主单查看详情
     * @param id
     * @return
     */
    List<TAppointmentPersonnelVO> getListById(Long id);

    /**
     * 通过id查询车辆详情
     * @param id
     * @return
     */
    List<TAppointmentVehicleVO> getVehicleListById(Long id);

    /**
     * 查询预约看板数据
     * @param data
     */
    PageResult<TAppointmentVO>  pageBoard(AppointmentDTO data);

    /**
     * 逻辑删除预约看板数据
     * @param id
     */
    void delAppointment(Long id);

    /**
     * 根据类型查询汇总数据
     * @param id
     * @param type
     */
    JSONObject appointmentSum(Long id, Long type);

    /**
     * 下发信息
     * @param data
     */
    void issuedPeople(JSONObject data);

    JSONArray querOtherAppointmentService(Long siteId, Integer page, Integer limit);

    /**
     * 导入车辆入场申请
     * @param file
     */
    List<TAppointmentVehicle> importData(MultipartFile file);

    com.alibaba.fastjson.JSONObject queryStatisticsallPeopleReservation();

    com.alibaba.fastjson.JSONObject queryTotalAppointments(Long siteId);

    com.alibaba.fastjson.JSONObject queryappointmentFormspecifyLicensePlatesAndEntourage(String plateNumber, String recordTime);
}