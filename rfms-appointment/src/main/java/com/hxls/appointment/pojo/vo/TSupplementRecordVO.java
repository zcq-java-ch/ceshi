package com.hxls.appointment.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "出入补录表")
public class TSupplementRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 对应站点id
     */
    private Long siteId;

    /**
     * 出入类型（数据字典）
     */
    private String accessType;

    /**
     * 出入通道
     */
    private String channel;

    /**
     * 补录类型（数据字典）
     */
    private String supplementType;

    /**
     * 补录时间
     */
    private Date supplementTime;

    /**
     * 表单信息
     */
    private Object remark;

}
