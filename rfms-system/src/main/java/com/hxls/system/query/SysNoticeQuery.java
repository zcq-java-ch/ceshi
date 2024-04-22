package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 系统消息表查询
*
* @author zhaohong 
* @since 1.0.0 2024-04-22
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "系统消息表查询")
public class SysNoticeQuery extends Query {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "公告标题")
    private String noticeTitle;

    @Schema(description = "接收人")
    private Long receiverId;

    @Schema(description = "阅读时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date[] readTime;

    @Schema(description = "状态 0:未读, 1:已读")
    private Integer status;

}
