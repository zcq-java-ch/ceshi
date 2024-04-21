package com.hxls.appointment.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fhs.core.trans.vo.TransPojo;
import com.hxls.framework.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "出入补录表")
public class TSupplementRecordExcelVO implements Serializable, TransPojo {

    private static final long serialVersionUID = 1L;


    /**
     * 对应厂站名称
     */
    @Schema(description = "厂站")
    @ExcelProperty("厂站")
    private String siteName;

    /**
     * 出入类型（数据字典）
     */
    @Schema(description = "出入类型（数据字典）")
    @ExcelProperty("出入类型")
    private String accessType;

    /**
     * 出入通道
     */
    @Schema(description = "区域")
    @ExcelProperty("区域")
    private String channel;

    /**
     * 补录类型（数据字典）
     */
    @Schema(description = "补录类型（数据字典）")
    @ExcelProperty("补录类型")
    private String supplementType;

    /**
     * 补录时间
     */
    @Schema(description = "记录时间")
    @ExcelProperty("补录时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date supplementTime;

    /**
     * 名称
     */
    @Schema(description = "姓名")
    @ExcelProperty("姓名")
    private String userName;



    /**
     * 组织名字
     */
    @Schema(description = "单位")
    @ExcelProperty("单位")
    private String orgName;


    /**
     * 带班负责人名字
     */
    @Schema(description = "代办负责人")
    @ExcelProperty("代办负责人")
    private String supervisorName;

    /**
     * 身份证号码
     */
    @Schema(description = "身份证号码")
    @ExcelProperty("身份证号码")
    private String idCardNumber;

    /**
     * 手机号码
     */
    @Schema(description = "手机号")
    @ExcelProperty("手机号")
    private String phone;


    /**
     * 岗位名字
     */
    @Schema(description = "岗位")
    @ExcelProperty("岗位")
    private String positionName;

}
