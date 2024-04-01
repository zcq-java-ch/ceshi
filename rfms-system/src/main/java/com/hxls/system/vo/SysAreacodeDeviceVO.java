package com.hxls.system.vo;

import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
* 区域通道随机码与设备中间表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Data
@Schema(description = "区域通道随机码与设备中间表")
public class SysAreacodeDeviceVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(description = "区域类型出入随机码")
	private String areaDeviceCode;

	@Schema(description = "关联设备id")
	private Long deviceId;

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