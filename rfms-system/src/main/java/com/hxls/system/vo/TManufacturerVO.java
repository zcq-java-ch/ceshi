package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 厂家管理表
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@Schema(description = "厂家管理表")
public class TManufacturerVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "厂家编码")
	private String manufacturerCode;

	@Schema(description = "厂家名称")
	private String manufacturerName;

	@Schema(description = "接口地址")
	private String interfaceAddress;

	@Schema(description = "端口号")
	private String portNumber;

	@Schema(description = "app_id")
	private String appId;

	@Schema(description = "secret")
	private String secret;

	@Schema(description = "备注")
	private String remark;

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