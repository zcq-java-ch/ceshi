package com.hxls.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
* 人员出入记录表
*
* @author zhaohong
* @since 1.0.0 2024-03-29
*/
@Data
@Schema(description = "人员出入记录表Fegin")
public class TPersonAccessRecordsVO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "对应站点id")
	private Long siteId;

	@Schema(description = "对应站点名字")
	private String siteName;

	@Schema(description = "对应厂商id")
	private Long manufacturerId;

//	@ExcelProperty("对应厂商名字")
	@Schema(description = "对应厂商名字")
	private String manufacturerName;

	@Schema(description = "出入类型  1：进场   2：出场")
	private String accessType;

	private String accessTypeLabel;

	@Schema(description = "出入通道区域ID")
	private Long channelId;

	@Schema(description = "出入通道区域名字")
	private String channelName;

	@Schema(description = "设备ID")
	private Long deviceId;

	@Schema(description = "设备名字")
	private String deviceName;

	@Schema(description = "记录时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recordTime;

	@Schema(description = "用户id")
	private Long personId;

	@Schema(description = "设备方用户唯一标识")
	private String devicePersonId;

	@Schema(description = "名字")
	private String personName;

	@Schema(description = "单位id")
	private Long companyId;

	@Schema(description = "单位")
	private String companyName;

	@Schema(description = "带班负责人id")
	private Long supervisorId;

	@Schema(description = "带班负责人名字")
	private String supervisorName;

	@Schema(description = "身份证号码")
	private String idCardNumber;

	@Schema(description = "手机号码")
	private String phone;

	@Schema(description = "头像地址")
	private String headUrl;

	@Schema(description = "岗位id")
	private Long positionId;

	@Schema(description = "岗位名字")
	private String positionName;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@Schema(description = "更新者")
	private Long updater;

	@Schema(description = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	@Schema(description = "记录唯一标识")
	private String recordsId;

	@Schema(description = "用户业务类型")
	private String busis;

//	@ExcelProperty(value = "用户业务类型")
	private String busisLabel;

	@Schema(description = "记录创建类型（0:自动生成 1:手动创建）")
	private String createType;

	private String createTypeLabel;

	private String directionType;


}
