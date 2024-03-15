package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 厂家管理表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "厂家管理表查询")
public class TManufacturerQuery extends Query {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "厂家编码")
    private String manufacturerCode;

    @Schema(description = "厂家名称")
    private String manufacturerName;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

}