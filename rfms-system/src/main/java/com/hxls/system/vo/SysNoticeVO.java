package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import com.hxls.framework.common.utils.DateUtils;
import java.util.Date;

/**
* 系统消息表
*
* @author zhaohong
* @since 1.0.0 2024-04-22
*/
@Data
@Schema(description = "系统消息表")
public class SysNoticeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "公告标题")
	private String noticeTitle;

	@Schema(description = "公告内容")
	private Object noticeContent;

	@Schema(description = "接收人")
	private Long receiverId;

	@Schema(description = "阅读时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date readTime;

	@Schema(description = "版本号")
	private Integer version;

	@Schema(description = "状态 0:未读, 1:已读")
	private Integer status;

	@Schema(description = "删除标识  0：正常   1：已删除")
	private Integer deleted;

	@Schema(description = "创建者")
	private Long creator;

	@Schema(description = "创建时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date createTime;

	@Schema(description = "更新者")
	private Long updater;

	@Schema(description = "更新时间")
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date updateTime;


}
