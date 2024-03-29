package com.hxls.datasection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 车辆出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Data
@Schema(description = "车辆出入记录表")
public class TVehicleAccessRecordsVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "对应站点id")
	private Long manufacturerId;

	@Schema(description = "对应站点名字")
	private String manufacturerName;

	@Schema(description = "出入类型（数据字典）")
	private String accessType;

	@Schema(description = "出入通道ID")
	private Long channelId;

	@Schema(description = "出入通道名字")
	private String channelName;

	@Schema(description = "记录时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date recordTime;

	@Schema(description = "车牌号")
	private String plateNumber;

	@Schema(description = "车型（数据字典）")
	private String vehicleModel;

	@Schema(description = "车辆排放标准（数据字典）")
	private String emissionStandard;

	@Schema(description = "车辆照片地址")
	private String carUrl;

	@Schema(description = "司机id")
	private Long driverId;

	@Schema(description = "司机姓名")
	private String driverName;

	@Schema(description = "司机手机号码")
	private String driverPhone;

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