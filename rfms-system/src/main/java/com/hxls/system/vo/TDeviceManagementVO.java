package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 设备管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@Data
@Schema(description = "设备管理表")
public class TDeviceManagementVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "id")
	private Long id;

	@Schema(description = "所属站点ID")
	private Long siteId;

	@Schema(description = "所属站点编码")
	private String siteCode;

	@Schema(description = "设备名称")
	private String deviceName;

	@Schema(description = "设备序列号")
	private String deviceSn;

	@Schema(description = "设备类型（数据字典）")
	private String deviceType;

	@Schema(description = "所属厂家")
	private Long manufacturerId;

	@Schema(description = "所属厂家编码")
	private String manufacturerCode;

	@Schema(description = "连接方式（数据字典）")
	private String connectionType;

	@Schema(description = "IP地址")
	private String ipAddress;

	@Schema(description = "登录账户")
	private String account;

	@Schema(description = "登录密码")
	private String password;

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

	@Schema(description = "进出类型（数据字典）")
	private String type;

	@Schema(description = "主机ip")
	private String masterIp;

	@Schema(description = "主机序列号")
	private String masterSn;

	@Schema(description = "主机账号")
	private String masterAccount;

	@Schema(description = "主机密码")
	private String masterPassword;


}
