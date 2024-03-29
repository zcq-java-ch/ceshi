package com.hxls.datasection.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
* 人员出入记录表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "人员出入记录表查询")
public class TPersonAccessRecordsQuery extends Query {

    @Schema(description = "对应站点id")
    private Long manufacturerId;

    @Schema(description = "出入类型（数据字典）")
    private String accessType;

    @Schema(description = "出入通道ID")
    private Long channelId;

    @Schema(description = "查询开始记录时间")
    private String startRecordTime;

    @Schema(description = "查询结束记录时间")
    private String endRecordTime;

    @Schema(description = "名字")
    private String personName;
}