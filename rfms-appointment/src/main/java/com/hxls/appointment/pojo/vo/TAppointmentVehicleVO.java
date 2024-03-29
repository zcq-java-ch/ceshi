package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 预约车辆信息表
 * @TableName t_appointment_vehicle
 */
@Data
public class TAppointmentVehicleVO implements Serializable {
    private static final long serialVersionUID = 1L;


//    /**
//     * id
//     */
//    @Schema(description = "id")
//    private Long id;
    /**
     * 对应预约单
     */
    @Schema(description = "对应预约单")
    private Long appointmentId;
    /**
     * 车牌号
     */
    @Schema(description = "车牌号")
    private String plateNumber;

    /**
     * 乘坐人
     */
    @Schema(description = "乘坐人")
    private String passenger;

    /**
     * 车辆照片地址
     */
    @Schema(description = "车辆照片地址")
    private String carUrl;

    /**
     * 车型（数据字典）
     */
    @Schema(description = "车型（数据字典）")
    private String vehicleModel;

    /**
     * 车辆排放标准（数据字典）
     */
    @Schema(description = "车辆排放标准（数据字典）")
    private String emissionStandard;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;


    /**
     * 状态 0:停用, 1:启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 删除标识  0：正常   1：已删除
     */
    private Integer deleted;

    /**
     * 创建者
     */
    private Long creator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private Long updater;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 送货日期
     */
    private String deliveryDate;


}