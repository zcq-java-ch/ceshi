package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TAppointmentPersonnelVO implements Serializable {

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
     * 对应补录单
     */
    private Long supplementaryId;

    /**
     * 预约人id
     */
    @Schema(description = "预约人id")
    private Long userId;


    @Schema(description = "外部人员名称")
    private String externalPersonnel;


    /**
     * 组织编码
     */
    @Schema(description = "组织编码")
    private String orgCode;

    /**
     * 组织名字
     */
    @Schema(description = "组织名字")
    private String orgName;

    /**
     * 带班负责人id
     */
    @Schema(description = "带班负责人id")
    private Long supervisorId;

    /**
     * 带班负责人名字
     */
    @Schema(description = "带班负责人名字")
    private String supervisorName;

    /**
     * 身份证号码
     */
    @Schema(description = "身份证号码")
    private String idCardNumber;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    private String phone;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址")
    private String headUrl;

    /**
     * 岗位id
     */
    @Schema(description = "岗位id")
    private Long positionId;

    /**
     * 岗位名字
     */
    @Schema(description = "岗位名字")
    private String positionName;

    /**
     * 车辆照片地址
     */
    @Schema(description = "车辆照片地址")
    private String carUrl;

    /**
     * 车牌号
     */
    @Schema(description = "车牌号")
    private String plateNumber;

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


    /**
     * 业务资源
     */
    private String busis;

    private Integer isCommit;
}
