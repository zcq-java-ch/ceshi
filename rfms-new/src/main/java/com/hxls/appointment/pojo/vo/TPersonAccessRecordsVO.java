package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员出入记录表
 * @TableName t_person_access_records
 */
@Data
public class TPersonAccessRecordsVO implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 对应站点id
     */
    @Schema(description = "对应站点id")
    private Long siteId;

    /**
     * 出入类型（数据字典）
     */
    @Schema(description = "出入类型（数据字典）")
    private String accessType;

    /**
     * 出入通道
     */
    @Schema(description = "出入通道")
    private String channel;

    /**
     * 记录时间
     */
    @Schema(description = "记录时间")
    private Date recordTime;

    /**
     * 名字
     */
    @Schema(description = "名字")
    private String personName;

    /**
     * 单位
     */
    @Schema(description = "单位")
    private String company;

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
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;


    /**
     * 状态 0:停用, 1:启用
     */
    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

    private static final long serialVersionUID = 1L;

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

}