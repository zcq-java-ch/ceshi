package com.hxls.appointment.pojo.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.common.query.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员出入记录表
 * @TableName t_person_access_records
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "预约信息表查询")
public class TPersonAccessRecordsQuery extends Query implements Serializable{
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
}