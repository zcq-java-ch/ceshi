package com.hxls.datasection.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

/**
 * 车辆进出厂展示台账
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-18
 */

@Data
@TableName("t_vehicle_access_ledger")
public class TVehicleAccessLedgerEntity {
	@TableId
	private Long id;

	/**
	* 对应站点id
	*/
	private Long siteId;

	/**
	* 对应站点名字
	*/
	private String siteName;

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
	* 行驶证照片
	*/
	private String licenseImage;

	/**
	* 环报随车清单
	*/
	private String envirList;

	/**
	* 车队名称
	*/
	private String fleetName;

	/**
	* 进厂时间
	*/
	private Date inTime;

	/**
	* 出厂时间
	*/
	private Date outTime;

	/**
	* 车辆识别码
	*/
	private String vinNumber;

	/**
	* 发动机号
	*/
	private String engineNumber;

	/**
	* 进厂照片
	*/
	private String inPic;

	/**
	* 出厂照片
	*/
	private String outPic;

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