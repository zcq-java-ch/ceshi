package com.hxls.system.vo;

import lombok.Data;

/**
 * @author zhaohong
 * @version 1.0
 * @class_name OrganizationVO
 * @create_date 2024/3/20 17:35
 * @description 华西主系统的组织信息vo
 */
@Data
public class OrganizationVO {
    /**
     * 所属公司编码
     */
    private String belongComCode;
    /**
     * 所属公司名称
     */
    private String belongComName;
    /**
     * 所属核算组织编码
     */
    private String belongOrgCode;
    /**
     * 所属核算组织名称
     */
    private String belongOrgName;
    /**
     * 所属法人
     */
    private String belongPerson;
    /**
     * 业务板块编码
     */
    private String busSegmentCodeCode;
    /**
     * 业务板块名称
     */
    private String busSegmentCodeName;
    /**
     * 业务板块取值
     */
    private String busSegmentCodeVals;
    /**
     * 城市编码
     */
    private String cityCodeCode;
    /**
     * 城市名称
     */
    private String cityCodeName;
    /**
     * 城市取值
     */
    private String cityCodeVals;
    /**
     * 行政组织编码（唯一）
     */
    private String code;
    /**
     * 公司详细地址
     */
    private String comAddres;
    /**
     * 分发类型，CREATE: 创建；UPDATE: 修改； FREEZE: 冻结； UNFREEZE: 活动
     */
    private String disType;
    /**
     * 地域经济圈编码
     */
    private String economicCircleCodeCode;
    /**
     * 地域经济圈名称
     */
    private String economicCircleCodeName;
    /**
     * 地域经济圈取值
     */
    private String economicCircleCodeVals;
    private Long endData;
    /**
     * 企业性质编码
     */
    private String enterNatureCodeCode;
    /**
     * 企业性质名称
     */
    private String enterNatureCodeName;
    /**
     * 企业性质取值
     */
    private String enterNatureCodeVals;
    /**
     * 成立日期
     */
    private String establishDate;
    /**
     * 自增流水号
     */
    private String id;
    /**
     * 行业编码
     */
    private String industryCodeCode;
    /**
     * 行业名称
     */
    private String industryCodeName;
    /**
     * 行业取值
     */
    private String industryCodeVals;
    /**
     * 是否法人公司，0：否 1：是
     */
    private String isLegalComVals;
    /**
     * 部门负责人编码(员工编码)
     */
    private String leaderCode;
    /**
     * 部门负责人名称(员工姓名)
     */
    private String leaderName;
    /**
     * 法定代表人
     */
    private String legalPerson;
    /**
     * 自增流水号（子系统自动生成流水号）
     */
    private String mainId;
    /**
     * 行政组织名称
     */
    private String name;
    /**
     * 排序字段
     */
    private String orderNum;
    /**
     * 页码
     */
    private Long page;
    /**
     * 每页条数
     */
    private Long pageSize;
    /**
     * 上级行政组织编码，如果是顶级，传空字符串。
     */
    private String pcode;
    /**
     * 上级行政组织名称，如果是顶级，传空字符串。
     */
    private String pname;
    /**
     * 单位类型（1：公司 2：部门）
     */
    private Long property;
    /**
     * 登记注册类型编码
     */
    private String registerTypeCodeCode;
    /**
     * 登记注册类型名称
     */
    private String registerTypeCodeName;
    /**
     * 登记注册类型取值
     */
    private String registerTypeCodeVals;
    /**
     * 统一社会信用代码/纳税人识别号
     */
    private String socialCreditCode;
    /**
     * 排序字段
     */
    private String sortProperty;
    /**
     * 排序类型 asc/desc
     */
    private String sortType;
    private Long startData;
    /**
     * 状态 0:停用, 1:启用
     */
    private Long status;
    /**
     * 组织类型编码
     */
    private String typeCodeCode;
    /**
     * 组织类型名称
     */
    private String typeCodeName;
    /**
     * 组织类型取值
     */
    private String typeCodeVals;
}
