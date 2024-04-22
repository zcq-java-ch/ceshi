package com.hxls.datasection.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
* 车辆进出厂展示台账查询
*
* @author zhaohong 
* @since 1.0.0 2024-04-18
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "车辆进出厂展示台账查询")
public class TVehicleAccessLedgerQuery extends Query {

    @Schema(description = "对应站点id")
    private Long siteId;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "进厂时间段")
    private List<String> inRecordTimeArr;

    @Schema(description = "出场时间段")
    private List<String> outRecordTimeArr;

    @Schema(description = "车型")
    private String vehicleModel;

    @Schema(description = "车队名称")
    private String fleetName;

}