package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 通用车辆管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@Data
@Schema(description = "通用车辆管理表")
public class TVehicleVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "站点ID")
	private Long siteId;

	@Schema(description = "站点名字")
	private String siteName;

	@Schema(description = "车牌号")
	private String licensePlate;

	@Schema(description = "车辆照片")
	private String imageUrl;

	@Schema(description = "排放标准（数据字典）")
	private String emissionStandard;

	@Schema(description = "默认司机（关联用户）")
	private Long driverId;

	@Schema(description = "默认司机姓名")
	private String driverName;

	@Schema(description = "默认司机手机号")
	private String driverPhone;

	@Schema(description = "默认司机手机号码")
	private String driverMobile;

	@Schema(description = "使用司机（关联用户）")
	private Long userId;

	@Schema(description = "车辆注册日期")
	private Date registrationDate;

	@Schema(description = "车辆识别码")
	private String vinNumber;

	@Schema(description = "发动机号")
	private String engineNumber;

	@Schema(description = "车队名称")
	private String fleetName;

	@Schema(description = "最大运输量")
	private String maxCapacity;

	@Schema(description = "行驶证照片")
	private String licenseImage;

	@Schema(description = "图片")
	private String images;

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

	@Schema(description = "车辆类型")
	private String carType;
}
