package com.hxls.appointment.pojo.vo.leadingVO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class recordInfo {


    /**
     * 运输量
     */
    @ExcelProperty("运输量")
    private BigDecimal freightVolume;

    /**
     * 第一次重量
     */
    private String firstWeight;
    private String receiveStationSite;
    /**
     * 出场时间
     */
    @ExcelProperty("出场时间")
    private String secondTime;
    @ExcelProperty("单据号")
    private String code;
    /**
     * 车牌号
     */
    @ExcelProperty("车牌号")
    private String carNum;
    /**
     * 物品名称
     */
    @ExcelProperty("运输货物")
    private String repertory;
    /**
     * 第二次重量
     */
    private String secondWeight;
    private String rawName;
    /**
     * 入场时间
     */
    @ExcelProperty("进场时间")
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
    @ExcelProperty("车队名称")
    private String cdName;
    /**
     * VIN号
     */
    @ExcelProperty("车牌识别号")
    private String VIN;
    /**
     * 发动机编号
     */
    @ExcelProperty("发动机号")
    private String engineNumber;
    /**
     * 排放标准
     */
    @ExcelProperty("排放等级")
    private String emissionStandard;

    /**
     * 行驶证路径
     */
    @ExcelProperty("行驶证")
    private String drivingLicense;

    /**
     * 随车清单路径
     */
    @ExcelProperty("车辆图片")
    private String followInventory;

    /**
     * 单位
     */
    @ExcelProperty("单位")
    private String unit;

    /**
     * 组织名称
     */
    @ExcelProperty("组织名称")
    private String orgName = "四川华西绿舍精城建材有限公司";
    /**
     * 组织编码
     */
    @ExcelProperty("组织编码")
    private String orgCode = "HXJCJC";

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private String creatTime;



}
