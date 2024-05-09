package com.hxls.appointment.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.hxls.framework.mybatis.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约人员信息表
 * @TableName t_appointment_personnel
 */
@TableName(value ="t_appointment_personnel")
@EqualsAndHashCode(callSuper=false)
@Data
public class TAppointmentPersonnel extends BaseEntity {


    /**
     * 对应预约单
     */
    private Long appointmentId;

    /**
     * 对应补录单
     */
    private Long supplementaryId;

    /**
     * 预约人id
     */
    private Long userId;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名字
     */
    private String orgName;

    /**
     * 外部人员名称
     */
    private String externalPersonnel;

    /**
     * 带班负责人id
     */
    private Long supervisorId;

    /**
     * 带班负责人名字
     */
    private String supervisorName;

    /**
     * 身份证号码
     */
    private String idCardNumber;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 头像地址
     */
    private String headUrl;

    /**
     * 岗位id
     */
    private Long positionId;

    /**
     * 岗位名字
     */
    private String positionName;

    /**
     * 车辆照片地址
     */
    private String carUrl;

    /**
     * 车牌号
     */
    private String plateNumber;

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

    /**
     * 岗位编码
     */
    private String postCode;


    @TableField(exist = false)
    private String stationId;
}
