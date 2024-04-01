package com.hxls.system.query;

import com.hxls.framework.common.query.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 区域通道随机码与设备中间表查询
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "区域通道随机码与设备中间表查询")
public class SysAreacodeDeviceQuery extends Query {
}