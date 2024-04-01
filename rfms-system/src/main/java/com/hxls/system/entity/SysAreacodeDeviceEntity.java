package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

/**
 * 区域通道随机码与设备中间表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */

@Data
@TableName("sys_areacode_device")
public class SysAreacodeDeviceEntity {
	@TableId
	private Long id;

	/**
	* 区域类型出入随机码
	*/
	private String areaDeviceCode;

	/**
	* 关联设备id
	*/
	private Long deviceId;

	/**
	* 排序
	*/
	private Integer sort;

	/**
	* 版本号
	*/
	private Integer version;

	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;

	/**
	* 删除标识  0：正常   1：已删除
	*/
	private Integer deleted;

	/**
	* 创建者
	*/
	private Long creator;

	/**
	* 创建时间
	*/
	private Date createTime;

	/**
	* 更新者
	*/
	private Long updater;

	/**
	* 更新时间
	*/
	private Date updateTime;

}