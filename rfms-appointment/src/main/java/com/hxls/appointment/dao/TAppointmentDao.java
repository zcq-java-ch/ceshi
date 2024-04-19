package com.hxls.appointment.dao;

import com.alibaba.fastjson.JSONObject;
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


   /**
    * 通过id查询名字
    */
   String getNameById(@Param("id") Long id);



   /**
    * 通过车牌获取所有的车辆信息
    * @param data 车牌集合
    * @return 车辆集合
    */
   List<TVehicleVO> listByCarNumber(@Param("strings")List<String> data);


   /**
    * 通过id查询人名和岗位名称
    */
   JSONObject selectRealNameById(@Param("id") Long id);


   /**
    * 通过id查询区域名称
    */
   String selectAreaNameById(@Param("id") Long id);


   /**
    * 通过id查询供应商名称
    */
   String selectSupplierNameById(@Param("id") Long id);

   /**
    * 通过id查询场站名称
    */
   String selectSiteNameById(@Param("id") Long id);


   /**
    * 通过id查询场站code
    */
   String selectSiteCodeById(@Param("id") Long id);

   /**
    * 通过id查询场站设备集合
    */
   List<String> selectManuFacturerIdById(@Param("id") Long id, @Param("type")String type);


   /**
    * 通过code查询场站主机ip
    */
   List<String> selectMasterIpById(@Param("code") String code ,@Param("type")String type);

   /**
    * 通过code查询场站设备的账号密码
    */
   List<JSONObject> selectDeviceList(@Param("code") String code);


}