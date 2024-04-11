package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "出入补录表")
public class TSupplementRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long id;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 对应站点id
     */
    @Schema(description = "站点id")
    private Long siteId;


    /**
     * 对应站点名称
     */
    @Schema(description = "站点名称")
    private String siteName;

    /**
     * 出入类型（数据字典）
     */
    @Schema(description = "出入类型（数据字典）")
    private String accessType;

    /**
     * 出入通道
     */
    @Schema(description = "区域")
    private String channel;

    /**
     * 补录类型（数据字典）
     */
    @Schema(description = "补录类型（数据字典）")
    private String supplementType;

    /**
     * 补录时间
     */
    @Schema(description = "记录时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date supplementTime;

    /**
     * 表单信息
     */
    @Schema(description = "表单信息")
    private List<TAppointmentPersonnelVO> remark1;

    private Boolean person = false;

    @Schema(description = "创建者")
    private Long creator;

    @Schema(description = "创建者名称")
    private String creatorName;

    @Schema(description = "创建者组织")
    private String submitterOrgName;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;


}
