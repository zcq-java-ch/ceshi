package com.hxls.datasection.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.framework.common.excel.DateConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
* 人员出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Data
@Schema(description = "人员出入记录表")
public class TPersonAccessRecordsVO implements Serializable, TransPojo {
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
	@Schema(description = "出入通道区域ID")
	private Long channelId;

	@ExcelProperty("出入通道区域名字")
	@Schema(description = "出入通道区域名字")
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

	@ExcelIgnore
	@Schema(description = "用户id")
	private Long personId;

	@ExcelIgnore
	@Schema(description = "设备方用户唯一标识")
	private String devicePersonId;

	@ExcelProperty("姓名")
	@Schema(description = "名字")
	private String personName;

	@ExcelIgnore
	@Schema(description = "单位id")
	private Long companyId;

	@ExcelProperty("单位")
	@Schema(description = "单位")
	private String companyName;

	@ExcelIgnore
	@Schema(description = "带班负责人id")
	private Long supervisorId;

	@ExcelProperty("带班负责人")
	@Schema(description = "带班负责人名字")
	private String supervisorName;

	@ExcelProperty("身份证号码")
	@Schema(description = "身份证号码")
	private String idCardNumber;

	@ExcelProperty("手机号码")
	@Schema(description = "手机号码")
	private String phone;

	@ExcelIgnore
	@Schema(description = "头像地址")
	private String headUrl;

	@ExcelIgnore
	@Schema(description = "岗位id")
	private Long positionId;

	@ExcelProperty("岗位")
	@Schema(description = "岗位名字")
	private String positionName;

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
	@Trans(type = TransType.DICTIONARY, key = "business", ref = "busisLabel")
	@Schema(description = "用户业务类型")
	private String busis;

	@ExcelIgnore
//	@ExcelProperty(value = "用户业务类型")
	private String busisLabel;

	@ExcelIgnore
	@Trans(type = TransType.DICTIONARY, key = "create_type", ref = "createTypeLabel")
	@Schema(description = "记录创建类型（0:自动生成 1:手动创建）")
	private String createType;

	@ExcelProperty(value = "记录创建类型")
	private String createTypeLabel;

	@ExcelIgnore
	private String directionType;

	@ExcelIgnore
	private List<TPersonAccessRecordsEntity> todayDetails;

}