package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 通用车辆管理表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-15
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "通用车辆管理表查询")
public class TVehicleQuery extends Query {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "站点ID")
    private Long siteId;

    @Schema(description = "车牌号")
    private String licensePlate;

    @Schema(description = "默认司机（关联用户）")
    private Long driverId;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

    @Schema(description = "车辆所属（数据字典）")
    private String carClass;


}