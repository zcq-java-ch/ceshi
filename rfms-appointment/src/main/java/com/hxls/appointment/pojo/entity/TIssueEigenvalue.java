package com.hxls.appointment.pojo.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;

import lombok.EqualsAndHashCode;

/**
 * 下发特征值表
 * @TableName t_issue_eigenvalue
 */
@EqualsAndHashCode(callSuper=false)
@TableName(value="t_issue_eigenvalue")
@Data
public class TIssueEigenvalue extends BaseEntity {

    /**
     * 下发站点名称
     */
    private String stationName;

    /**
     * 下发站点id
     */
    private Long stationId;

    /**
     * 下发区域名称
     */
    private String areaName;

    /**
     * 下发区域id
     */
    private Long areaId;

    /**
     * 下发设备名称
     */
    private String deviceName;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 下发类型（1为人员 ， 2为车辆）
     */
    private Integer type;

    /**
     * 下发数据
     */
    private String data;

    /**
     * 状态  0：停用   1：正常
     */
    private Integer status;


}