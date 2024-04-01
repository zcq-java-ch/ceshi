package com.hxls.system.vo;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
* 站点区域表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Data
@Schema(description = "站点区域表")
public class SysSiteAreaVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(description = "关联站点id")
	private Long siteId;

	@Schema(description = "关联站点名字")
	private String siteName;

	@Schema(description = "区域名字")
	private String areaName;

	@Schema(description = "绑定人员入口随机码")
	private String faceInCode;

	@Schema(description = "绑定人员出口随机码")
	private String faceOutCode;

	@Schema(description = "绑定车辆入口随机码")
	private String carIntCode;

	@Schema(description = "绑定车辆出口随机码")
	private String carOutCode;

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

	@Schema(description = "绑定人员入口关联设备")
	private JSONArray faceInCodeAndDevices;

	@Schema(description = "绑定人员出口关联设备")
	private JSONArray faceOutCodeAndDevices;

	@Schema(description = "绑定车辆入口关联设备")
	private JSONArray carIntCodeAndDevices;

	@Schema(description = "绑定车辆出口关联设备")
	private JSONArray carOutCodeAndDevices;

	@Schema(description = "新增绑定人员入口关联设备")
	private List<Long> faceInCodeAddDevices;

	@Schema(description = "新增绑定人员出口关联设备")
	private List<Long> faceOutCodeAddDevices;

	@Schema(description = "新增绑定车辆入口关联设备")
	private List<Long> carIntCodeAddDevices;

	@Schema(description = "新增绑定车辆出口关联设备")
	private List<Long> carOutCodeAddDevices;

}