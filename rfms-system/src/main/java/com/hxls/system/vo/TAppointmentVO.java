package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 预约信息表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@Schema(description = "预约信息表")
public class TAppointmentVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "预约类型（数据字典）")
	private String appointmentType;

	@Schema(description = "提交人")
	private String submitter;

	@Schema(description = "预约站点id")
	private Long siteId;

	@Schema(description = "预约厂站名字")
	private String siteName;

	@Schema(description = "预约事由")
	private String purpose;

	@Schema(description = "预约访问开始时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date startTime;

	@Schema(description = "预约访问结束时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date endTime;

	@Schema(description = "审核时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date reviewTime;

	@Schema(description = "审核结果")
	private String reviewResult;

	@Schema(description = "审核状态（数据字典）")
	private String reviewStatus;

	@Schema(description = "排序")
	private Integer sort;

	@Schema(description = "版本号")
	private Integer version;

	@Schema(description = "状态 0:停用, 1:启用")
	private Integer status;

	@Schema(description = "删除标识  0：正常   1：已删除")
	private Integer deleted;

	@Schema(description = "创建者")
	private Long creator;

	@Schema(description = "创建时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date createTime;

	@Schema(description = "更新者")
	private Long updater;

	@Schema(description = "更新时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date updateTime;


}