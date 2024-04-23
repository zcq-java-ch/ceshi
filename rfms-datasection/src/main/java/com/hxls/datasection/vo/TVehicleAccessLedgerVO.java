package com.hxls.datasection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 车辆进出厂展示台账
*
* @author zhaohong 
* @since 1.0.0 2024-04-18
*/
@Data
@Schema(description = "车辆进出厂展示台账")
public class TVehicleAccessLedgerVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(description = "对应站点id")
	private Long siteId;

	@Schema(description = "对应站点名字")
	private String siteName;

	@Schema(description = "车牌号")
	private String plateNumber;

	@Schema(description = "车型（数据字典）")
	private String vehicleModel;

	@Schema(description = "车辆排放标准（数据字典）")
	private String emissionStandard;

	@Schema(description = "行驶证照片")
	private String licenseImage;

	@Schema(description = "环报随车清单")
	private String envirList;

	@Schema(description = "车队名称")
	private String fleetName;

	@Schema(description = "进厂时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date inTime;

	@Schema(description = "出厂时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date outTime;

	@Schema(description = "车辆识别码")
	private String vinNumber;

	@Schema(description = "发动机号")
	private String engineNumber;

	@Schema(description = "进厂照片")
	private String inPic;

	@Schema(description = "出厂照片")
	private String outPic;

	@Schema(description = "是否完成记录(0；否只有入 1：完成)")
	private Integer isOver;

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