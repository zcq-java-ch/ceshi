package com.hxls.appointment.pojo.query;

import com.hxls.framework.common.query.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "出入补录查询")
public class TSupplementRecordQuery extends Query {

    /**
     * 对应站点id
     */
    @Schema(description = "对应站点id")
    private Long siteId;

    private List<Long> siteIds;

    /**
     * 出入类型（数据字典）
     */
    @Schema(description = "出入类型（数据字典）")
    private String accessType;

    /**
     * 补录类型（数据字典）
     */
    @Schema(description = "补录类型（数据字典）")
    private String supplementType;

    /**
     * 出入通道
     */
    @Schema(description = "出入通道")
    private String channel;

    /**
     * 补录时间
     */
    @Schema(description = "补录时间")
    private String[] supplementTime;

    private Long creator;


}
