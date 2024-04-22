package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 系统消息表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-22
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("sys_notice")
public class SysNoticeEntity extends BaseEntity {

	/**
	* 公告标题
	*/
	private String noticeTitle;

	/**
	* 公告内容
	*/
	private Object noticeContent;

	/**
	* 接收人
	*/
	private Long receiverId;

	/**
	* 阅读时间
	*/
	private Date readTime;


	/**
	* 状态 0:未读, 1:已读
	*/
	private Integer status;






}
