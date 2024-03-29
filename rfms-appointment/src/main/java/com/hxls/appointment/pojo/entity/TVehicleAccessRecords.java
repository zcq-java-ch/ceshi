package com.hxls.appointment.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 车辆出入记录表
 * @TableName t_vehicle_access_records
 */
@TableName(value ="t_vehicle_access_records")
@Data
public class TVehicleAccessRecords implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应站点id
     */
    @TableField(value = "site_id")
    private Long siteId;

    /**
     * 出入类型（数据字典）
     */
    @TableField(value = "access_type")
    private String accessType;

    /**
     * 出入通道
     */
    @TableField(value = "channel")
    private String channel;

    /**
     * 记录时间
     */
    @TableField(value = "record_time")
    private Date recordTime;

    /**
     * 车牌号
     */
    @TableField(value = "plate_number")
    private String plateNumber;

    /**
     * 车型（数据字典）
     */
    @TableField(value = "vehicle_model")
    private String vehicleModel;

    /**
     * 车辆排放标准（数据字典）
     */
    @TableField(value = "emission_standard")
    private String emissionStandard;

    /**
     * 车辆照片地址
     */
    @TableField(value = "car_url")
    private String carUrl;

    /**
     * 司机姓名
     */
    @TableField(value = "driver_name")
    private String driverName;

    /**
     * 司机手机号码
     */
    @TableField(value = "driver_phone")
    private String driverPhone;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 状态 0:停用, 1:启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 删除标识  0：正常   1：已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    /**
     * 创建者
     */
    @TableField(value = "creator")
    private Long creator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "updater")
    private Long updater;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}