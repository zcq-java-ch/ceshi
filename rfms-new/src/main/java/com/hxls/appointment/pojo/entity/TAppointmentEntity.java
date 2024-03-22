package com.hxls.appointment.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 预约信息表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_appointment")
public class TAppointmentEntity extends BaseEntity {

	/**
	* 预约类型（数据字典）
	*/
	private String appointmentType;

	/**
	* 提交人
	*/
	private String submitter;

	/**
	* 预约站点id
	*/
	private Long siteId;

	/**
	* 预约厂站名字
	*/
	private String siteName;

	/**
	* 预约事由
	*/
	private String purpose;

	/**
	* 预约访问开始时间
	*/
	private Date startTime;

	/**
	* 预约访问结束时间
	*/
	private Date endTime;

	/**
	* 审核时间
	*/
	private Date reviewTime;

	/**
	* 审核结果
	*/
	private String reviewResult;

	/**
	* 审核状态（数据字典）
	*/
	private String reviewStatus;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;

}