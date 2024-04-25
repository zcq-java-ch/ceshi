package com.hxls.datasection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 车辆出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_vehicle_access_records")
public class TVehicleAccessRecordsEntity extends BaseEntity {
	/**
	 * 对应站点id
	 */
	private Long siteId;

	/**
	 * 对应站点名字
	 */
	private String siteName;
	/**
	* 对应厂商id
	*/
	private Long manufacturerId;

	/**
	* 对应厂商名字
	*/
	private String manufacturerName;

	/**
	* 出入类型（数据字典）
	*/
	private String accessType;

	/**
	* 出入通道ID
	*/
	private Long channelId;

	/**
	* 出入通道名字
	*/
	private String channelName;

	@Schema(description = "设备ID")
	private Long deviceId;

	@Schema(description = "设备名字")
	private String deviceName;



	/**
	* 记录时间
	*/
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date recordTime;

	/**
	* 车牌号
	*/
	private String plateNumber;

	/**
	* 车型（数据字典）
	*/
	private String vehicleModel;

	/**
	* 车辆排放标准（数据字典）
	*/
	private String emissionStandard;

	/**
	* 车辆照片地址
	*/
	private String carUrl;

	/**
	* 司机id
	*/
	private Long driverId;

	/**
	* 司机姓名
	*/
	private String driverName;

	/**
	* 司机手机号码
	*/
	private String driverPhone;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;

	/**
	 * 记录唯一标识
	 * */
	private String recordsId;

	/**
	 * 创建类型（0:自动生成 1:手动创建）
	 * */
	private String createType;

	/**
	 * 车辆默认照片
	 * */
	private String imageUrl;


}