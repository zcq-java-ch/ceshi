package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

/**
 * 站点区域表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */

@Data
@TableName("sys_site_area")
public class SysSiteAreaEntity {
	@TableId
	private Long id;

	/**
	* 关联站点id
	*/
	private Long siteId;

	/**
	* 关联站点名字
	*/
	private String siteName;

	/**
	* 区域名字
	*/
	private String areaName;

	/**
	* 绑定人员入口随机码
	*/
	private String faceInCode;

	/**
	* 绑定人员出口随机码
	*/
	private String faceOutCode;

	/**
	* 绑定车辆入口随机码
	*/
	private String carIntCode;

	/**
	* 绑定车辆出口随机码
	*/
	private String carOutCode;

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