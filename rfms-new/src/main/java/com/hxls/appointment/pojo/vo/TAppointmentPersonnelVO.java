package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TAppointmentPersonnelVO implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对应预约单
     */
    private Long appointmentId;

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
     * 版本号
     */
    private Integer version;

    /**
     * 状态 0:停用, 1:启用
     */
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
