package com.hxls.system.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 
 * @TableName sys_control_car
 */
@TableName("sys_control_car")
@Data
@EqualsAndHashCode(callSuper = false)
public class SysControlCar extends BaseEntity implements Serializable{

    private Long id;

    /**
     * 站点id
     */
    private Long siteId;

    /**
     * 站点名字
     */
    private String siteName;

    /**
     * 车牌号
     */
    private String licensePlate;
    /**
     * 类型 1为常规 ， 2为预约
     */
    private Integer type;


    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private Long personId;

    @TableField(exist = false)
    private Long stationId;

    @TableField(exist = false)
    private String isControl;


    private static final long serialVersionUID = 1L;
}