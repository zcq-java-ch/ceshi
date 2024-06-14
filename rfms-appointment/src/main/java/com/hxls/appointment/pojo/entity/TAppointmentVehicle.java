package com.hxls.appointment.pojo.entity;


import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约车辆信息表
 * @TableName t_appointment_vehicle
 */
@TableName(value ="t_appointment_vehicle")
@Data
@EqualsAndHashCode(callSuper=false)
public class TAppointmentVehicle extends BaseEntity {

    /**
     * 对应预约单
     */
    private Long appointmentId;
    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 送货日期
     */
    private String deliveryDate;

    /**
     * 乘坐人
     */
    private String passenger;

    /**
     * 车辆照片地址
     */
    private String carUrl;

    /**
     * 车型（数据字典）
     */
    private String vehicleModel;

    /**
     * 车辆排放标准（数据字典）
     */
    private String emissionStandard;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * 状态 0:停用, 1:启用
     */
    private Integer status;

    @TableField(exist = false)
    private String stationId;

}
