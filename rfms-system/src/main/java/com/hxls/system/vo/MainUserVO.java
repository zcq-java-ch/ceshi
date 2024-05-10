package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zhaohong
 * @version 1.0
 * @class_name MainUserVO
 * @create_date 2024/3/29 16:37
 * @description
 */
@Data
public class MainUserVO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "银行账户")
    private String bankAccount;

    @Schema(description = "银行账户名称")
    private String bankAccountName;

    @Schema(description = "银行联号")
    private String bankCode;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "员工编码")
    private String code;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "分发类型")
    private String disType;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "结束时间")
    private Long endData;

    @Schema(description = "身份证号")
    private String idNum;

    @Schema(description = "入职日期")
    private String inductionComDate;

    @Schema(description = "入司日期")
    private String jonComDate;


    @Schema(description = "常住地编码")
    private String locationCodeCode;

    @Schema(description = "常住地名称")
    private String locationCodeName;

    @Schema(description = "常住地取值")
    private String locationCodeVals;

    @Schema(description = "自增流水号（子系统自动生成流水号）")
    private String mainId;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "国籍编码")
    private String nationalityCodeCode;

    @Schema(description = "国籍名称")
    private String nationalityCodeName;

    @Schema(description = "国籍取值")
    private String nationalityCodeVals;

    @Schema(description = "民族编码")
    private String nationCodeCode;

    @Schema(description = "民族名称")
    private String nationCodeName;

    @Schema(description = "民族取值")
    private String nationCodeVals;


    @Schema(description = "籍贯")
    private String nativePlace;


    @Schema(description = "员工标识id")
    private String openId;


    @Schema(description = "页码")
    private Long page;

    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "护照号")
    private String passport;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "政治面貌编码")
    private String politicsCode;

    @Schema(description = "政治面貌名称")
    private String politicsName;

    @Schema(description = "政治面貌取值")
    private String politicVals;

    @Schema(description = "用工关系编码")
    private String relationStatusCode;

    @Schema(description = "用工关系名称")
    private String relationStatusName;

    @Schema(description = "用工关系状态")
    private String relationStatusVals;

    @Schema(description = "员工性别")
    private String sex;

    @Schema(description = "排序字段")
    private String sortProperty;

    @Schema(description = "排序类型 asc/desc")
    private String sortType;

    @Schema(description = "开始日期")
    private Long startData;

    @Schema(description = "状态")
    private Long status;


    @Schema(description = "办公电话")
    private String telephone;

    @Schema(description = "组织信息")
    private List<Map<String,String>> sysRefs;
}
