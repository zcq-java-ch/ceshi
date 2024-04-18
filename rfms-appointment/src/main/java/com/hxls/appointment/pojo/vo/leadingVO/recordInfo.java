package com.hxls.appointment.pojo.vo.leadingVO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class recordInfo {


    /**
     * 运输量
     */
    private BigDecimal freightVolume;

    /**
     * 第一次重量
     */
    private String firstWeight;
    private String receiveStationSite;
    /**
     * 出场时间
     */
    private String secondTime;
    private String code;
    /**
     * 车牌号
     */
    private String carNum;
    /**
     * 物品名称
     */
    private String repertory;
    /**
     * 第二次重量
     */
    private String secondWeight;
    private String rawName;
    /**
     * 入场时间
     */
    private String firstTime;
    private String netWeight;
    private String secondWeightNumber;
    private String firstWeightNumber;
    private String supplier;
    /**
     * 注册时间
     */
    private String zcTime;
    /**
     * 车队名称
     */
    private String cdName;
    /**
     * VIN号
     */
    private String VIN;
    /**
     * 发动机编号
     */
    private String engineNumber;
    /**
     * 排放标准
     */
    private String emissionStandard;

    /**
     * 行驶证路径
     */
    private String drivingLicense;

    /**
     * 随车清单路径
     */
    private String followInventory;

    /**
     * 单位
     */
    private String unit;

    /**
     * 组织名称
     */
    private String orgName = "四川华西绿舍精城建材有限公司";
    /**
     * 组织编码
     */
    private String orgCode = "HXJCJC";

    /**
     * 创建时间
     */
    private String creatTime;



}
