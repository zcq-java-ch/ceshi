package com.hxls.datasection.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 车辆出入记录表查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "车辆出入记录表查询")
public class TVehicleAccessRecordsQuery extends Query {
    @Schema(description = "对应站点id")
    private Long siteId;

    @Schema(description = "对应站点名字")
    private String siteName;

    @Schema(description = "出入类型（数据字典）")
    private String accessType;

    @Schema(description = "出入通道ID")
    private Long channelId;

    @Schema(description = "出入通道名字")
    private String channelName;

    @Schema(description = "记录时间")
    private String startRecordTime;

    @Schema(description = "记录时间")
    private String endRecordTime;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "司机id")
    private Long driverId;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "司机手机号码")
    private String driverPhone;


    @Schema(description = "车辆类型")
    private String vehicleModel;

}