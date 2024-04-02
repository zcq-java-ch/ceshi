package com.hxls.datasection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 人员出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_person_access_records")
public class TPersonAccessRecordsEntity extends BaseEntity {

	/**
	* 对应站点id
	*/
	private Long manufacturerId;

	/**
	* 对应站点名字
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

	private Long deviceId;

	private String deviceName;

	/**
	* 记录时间
	*/
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date recordTime;

	/**
	* 用户id
	*/
	private Long personId;

	/**
	* 名字
	*/
	private String personName;

	/**
	* 单位id
	*/
	private Long companyId;

	/**
	* 单位
	*/
	private String companyName;

	/**
	* 带班负责人id
	*/
	private Long supervisorId;

	/**
	* 带班负责人名字
	*/
	private String supervisorName;

	/**
	* 身份证号码
	*/
	private String idCardNumber;

	/**
	* 手机号码
	*/
	private String phone;

	/**
	* 头像地址
	*/
	private String headUrl;

	/**
	* 岗位id
	*/
	private Long positionId;

	/**
	* 岗位名字
	*/
	private String positionName;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;






}