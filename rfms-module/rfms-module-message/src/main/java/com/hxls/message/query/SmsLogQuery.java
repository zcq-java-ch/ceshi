package com.hxls.message.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;

/**
* 短信日志查询
*
* @author
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "短信日志查询")
public class SmsLogQuery extends Query {
    @Schema(description = "平台ID")
    private Long platformId;

    @Schema(description = "平台类型")
    private Integer platform;

}
