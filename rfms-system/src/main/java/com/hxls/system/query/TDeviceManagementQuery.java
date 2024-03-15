package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 设备管理表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备管理表查询")
public class TDeviceManagementQuery extends Query {
    @Schema(description = "所属站点ID")
    private Long siteId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "设备类型（数据字典）")
    private String deviceType;

    @Schema(description = "所属厂家")
    private Long manufacturerId;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

}