package com.hxls.appointment.dao;

import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import com.hxls.framework.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 预约信息表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TAppointmentDao extends BaseDao<TAppointmentEntity> {

   List<TVehicleVO> listByCarNumber(@Param("strings")List<String> data);
}