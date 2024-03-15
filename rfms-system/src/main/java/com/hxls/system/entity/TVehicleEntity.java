package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 通用车辆管理表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_vehicle")
public class TVehicleEntity extends BaseEntity {

	/**
	* 站点ID
	*/
	private Long siteId;

	/**
	* 车牌号
	*/
	private String licensePlate;

	/**
	* 车辆照片
	*/
	private String imageUrl;

	/**
	* 排放标准（数据字典）
	*/
	private String emissionStandard;

	/**
	* 默认司机（关联用户）
	*/
	private Long driverId;

	/**
	* 使用司机（关联用户）
	*/
	private Long userId;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;






}