package com.hxls.message.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;

/**
* 短信平台查询
*
* @author
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "短信平台查询")
public class SmsPlatformQuery extends Query {
    @Schema(description = "平台类型  0：阿里云   1：腾讯云   2：七牛云   3：华为云")
    private Integer platform;

    @Schema(description = "短信签名")
    private String signName;

}
