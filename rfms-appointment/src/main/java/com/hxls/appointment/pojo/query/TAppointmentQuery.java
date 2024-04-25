package com.hxls.appointment.pojo.query;

import com.hxls.framework.common.query.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
* 预约信息表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "预约信息表查询")
public class TAppointmentQuery extends Query {
    @Schema(description = "预约类型（数据字典）")
    private String appointmentType;

    @Schema(description = "供应商小类（ 0：人  ； 1：车 ），选择供应商的时候选择")
    private Integer supplierSubclass;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "提交人")
    private Long submitter;

    @Schema(description = "提交人姓名")
    private String submitterName;

    @Schema(description = "预约站点id")
    private Long siteId;
    @Schema(description = "预约站点id集合")
    private List<Long> siteIds;

    @Schema(description = "预约厂站名字")
    private String siteName;

    @Schema(description = "预约访问开始时间")
    private String startTime;

    @Schema(description = "预约访问结束时间")
    private String endTime;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private  String[] reviewTime;

    @Schema(description = "审核结果")
    private String reviewResult;

    @Schema(description = "审核状态（数据字典）")
    private String reviewStatus;

    @Schema(description = "提交时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private  String[] creatTime;


    /**
     * 外部预约存储的openId
     */
    @Schema(description = "外部预约存储的openId")
    private String openId;

    private Long id;

    private Long creator;

    /**
     * 是否由小程序查询外部预约
     */
    private Boolean other = false ;

    /**
     * 是否是人员审核
     */
    private Boolean isPerson = false;

    /**
     * 是否办理
     */
    private Boolean isFinish ;


    private Long userId;



}