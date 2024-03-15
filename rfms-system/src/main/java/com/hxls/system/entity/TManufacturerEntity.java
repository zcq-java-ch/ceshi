package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 厂家管理表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_manufacturer")
public class TManufacturerEntity extends BaseEntity {

	/**
	* 厂家编码
	*/
	private String manufacturerCode;

	/**
	* 厂家名称
	*/
	private String manufacturerName;

	/**
	* 接口地址
	*/
	private String interfaceAddress;

	/**
	* 端口号
	*/
	private String portNumber;

	/**
	* app_id
	*/
	private String appId;

	/**
	* secret
	*/
	private String secret;

	/**
	* 备注
	*/
	private String remark;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;






}