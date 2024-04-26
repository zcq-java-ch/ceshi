package com.hxls.system.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * excel供应商用户表
 *
 * @author
 *
 */
@Data
public class SysUserGysExcelVO implements Serializable, TransPojo {
    private static final long serialVersionUID = 1L;
    /**
     * 本属性对于导出无用，只是用于翻译
     */
    @ExcelIgnore
    private Long id;

    @ExcelProperty("代班负责人")
    private String  supervisor;

    @ExcelProperty("姓名")
    private String realName;


    @Schema(description = "身份证号码")
    private String idCard;


    @ExcelProperty("手机号")
    private String mobile;


    @ExcelIgnore
    @Trans(type = TransType.DICTIONARY, key = "quarters_data", ref = "postName")
    private Integer postId;

    @ExcelProperty(value = "岗位")
    private String postName;

    @Schema(description = "人像")
    private String avatar;


    @Schema(description = "车辆照片")
    private String imageUrl;


    @Schema(description = "车牌号")
    private String licensePlate;


    @ExcelIgnore
    @Trans(type = TransType.DICTIONARY, key = "car_type", ref = "carTypeName")
    private String carType;

    @ExcelProperty("车型")
    private String carTypeName;

    @ExcelIgnore
    @Trans(type = TransType.DICTIONARY, key = "emission_standard", ref = "emissionStandardName")
    private String emissionStandard;


    @ExcelProperty("排放标准")
    private String emissionStandardName;

}
