package com.hxls.system.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
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
	 * 默认司机姓名
	 */
	private String driverName;

	/**
	 * 默认司机手机号
	 */
	private String driverMobile;

	/**
	* 使用司机（关联用户）
	*/
	private Long userId;

	/**
	* 车辆注册日期
	*/
	private Date registrationDate;

	/**
	 * 车辆识别码
	 */
	private String vinNumber;

	/**
	 * 发动机号
	 */
	private String engineNumber;

	/**
	 * 车队名称
	 */
	private String fleetName;

	/**
	 * 最大运输量
	 */
	private String maxCapacity;

	/**
	 * 行驶证照片
	 */
	private String licenseImage;

	/**
	 * 图片
	 */
	private String images;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;


	/**
	 * 车辆类型（数据字典）
	 */
	private String carType;


	/**
	 * 车辆所属（数据字典）
	 */
	private String carClass;


	/**
	 * 供应商ID
	 */
	private Long supplierId;


	/**
	 * 供应商名字
	 */
	private String supplierName;

	/**
	 * 运输量
	 * */
	private String transportVolume;

	/**
	 * 运输货物
	 * */
	private String transportGoods;


	@TableField(exist = false)
	private Long stationId;

}
