package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 设备管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_device_management")
public class TDeviceManagementEntity extends BaseEntity {

	/**
	* 所属站点ID
	*/
	private Long siteId;

	/**
	 * 所属站点编码
	 */
	private String siteCode;

	/**
	 * 所属站点名称
	 */
	private String siteName;

	/**
	* 设备名称
	*/
	private String deviceName;

	/**
	* 设备序列号
	*/
	private String deviceSn;

	/**
	 * 进出类型（数据字典）
	 */
	private String type;

	/**
	 * 主机ip
	 */
	private String masterIp;

	/**
	 * 主机序列号
	 */
	private String masterSn;

	/**
	 * 主机账号
	 */
	private String masterAccount;

	/**
	 * 主机密码
	 */
	private String masterPassword;


	/**
	* 设备类型（数据字典）
	*/
	private String deviceType;

	/**
	* 所属厂家
	*/
	private Long manufacturerId;

	/**
	 * 所属厂站名称
	 */
	private String manufacturerName;

	/**
	 * 所属厂家编码
	 */
	private String manufacturerCode;

	/**
	* 连接方式（数据字典）
	*/
	private String connectionType;

	/**
	* IP地址
	*/
	private String ipAddress;

	/**
	* 登录账户
	*/
	private String account;

	/**
	* 登录密码
	*/
	private String password;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;






}
