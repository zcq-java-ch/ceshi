package com.hxls.datasection.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.hxls.framework.common.excel.DateConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
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
	@Serial
	private static final long serialVersionUID = 1L;

	@ExcelIgnore
	@Schema(description = "id")
	private Long id;

	@ExcelIgnore
	@Schema(description = "对应站点id")
	private Long siteId;

	@ExcelProperty("厂站")
	@Schema(description = "对应站点名字")
	private String siteName;

	@ExcelIgnore
	@Schema(description = "对应厂商id")
	private Long manufacturerId;

	@ExcelIgnore
//	@ExcelProperty("对应厂商名字")
	@Schema(description = "对应厂商名字")
	private String manufacturerName;

	@ExcelIgnore
	@Trans(type = TransType.DICTIONARY, key = "access_type", ref = "accessTypeLabel")
	@Schema(description = "出入类型  1：进场   2：出场")
	private String accessType;

	@ExcelProperty(value = "出入类型")
	private String accessTypeLabel;

	@ExcelIgnore
	@Schema(description = "出入通道ID")
	private Long channelId;

	@ExcelProperty("区域")
	@Schema(description = "出入通道名字")
	private String channelName;

	@ExcelIgnore
	@Schema(description = "设备ID")
	private Long deviceId;

	@ExcelProperty("设备名字")
	@Schema(description = "设备名字")
	private String deviceName;

	@ExcelProperty(value = "进出时间", converter = DateConverter.class)
	@Schema(description = "记录时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date recordTime;

	@ExcelProperty("车牌号")
	@Schema(description = "车牌号")
	private String plateNumber;

	@ExcelIgnore
	@Schema(description = "车型（数据字典）")
	private String vehicleModel;

	@ExcelProperty("车型")
	private String vehicleModelLab;

	@ExcelIgnore
	@Schema(description = "车辆排放标准（数据字典）")
	private String emissionStandard;

	@ExcelProperty("车辆排放标准")
	private String emissionStandardLab;

	@ExcelIgnore
	@Schema(description = "车辆照片地址")
	private String carUrl;

	@ExcelIgnore
	@Schema(description = "司机id")
	private Long driverId;

	@ExcelProperty("司机姓名")
	@Schema(description = "司机姓名")
	private String driverName;

	@ExcelProperty("司机手机号码")
	@Schema(description = "司机手机号码")
	private String driverPhone;

	@ExcelIgnore
	@Schema(description = "排序")
	private Integer sort;

	@ExcelIgnore
	@Schema(description = "版本号")
	private Integer version;

	@ExcelIgnore
	@Schema(description = "状态 0:停用, 1:启用")
	private Integer status;

	@ExcelIgnore
	@Schema(description = "删除标识  0：正常   1：已删除")
	private Integer deleted;

	@ExcelIgnore
	@Schema(description = "创建者")
	private Long creator;

	@ExcelIgnore
	@Schema(description = "创建时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date createTime;

	@ExcelIgnore
	@Schema(description = "更新者")
	private Long updater;

	@ExcelIgnore
	@Schema(description = "更新时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date updateTime;

	@ExcelIgnore
	@Schema(description = "记录唯一标识")
	private String recordsId;

	@ExcelIgnore
	@Trans(type = TransType.DICTIONARY, key = "create_type", ref = "createTypeLabel")
	@Schema(description = "记录创建类型（0:自动生成 1:手动创建）")
	private String createType;

	@ExcelProperty(value = "记录创建类型")
	private String createTypeLabel;


	@ExcelIgnore
	@Schema(description = "车辆默认照片")
	private String imageUrl;

	@ExcelIgnore
	@Schema(description = "行驶证照片")
	private String licenseImage;
}