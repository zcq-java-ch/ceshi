package com.hxls.appointment.pojo.vo.leadingVO;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class recordInfo {


    /**
     * 运输量
     */
    @ExcelProperty(value = "运输量" ,order = 10)
    private BigDecimal freightVolume;

    /**
     * 第一次重量
     */
    @ExcelIgnore
    private String firstWeight;
    @ExcelIgnore
    private String receiveStationSite;
    /**
     * 出场时间
     */
    @ExcelProperty(value = "出场时间",order = 4)
    private String secondTime;
    @ExcelProperty(value = "单据号",order = 1)
    private String code;
    /**
     * 车牌号
     */
    @ExcelProperty(value = "车牌号",order = 2)
    private String carNum;
    /**
     * 物品名称
     */
    @ExcelProperty(value = "运输货物" , order = 9)
    private String repertory;
    /**
     * 第二次重量
     */
    @ExcelIgnore
    private String secondWeight;
    @ExcelIgnore
    private String rawName;
    /**
     * 入场时间
     */
    @ExcelProperty(value = "进场时间" , order = 3)
    private String firstTime;
    @ExcelIgnore
    private String netWeight;
    @ExcelIgnore
    private String secondWeightNumber;
    @ExcelIgnore
    private String firstWeightNumber;
    @ExcelIgnore
    private String supplier;
    /**
     * 注册时间
     */
    @ExcelIgnore
    private String zcTime;
    /**
     * 车队名称
     */
    @ExcelProperty(value = "车队名称" , order = 12)
    private String cdName;
    /**
     * VIN号
     */
    @ExcelProperty(value = "车牌识别号" , order = 5)
    private String VIN;

    /**
     * VIN号
     */
    @ExcelProperty(value = "车辆注册日期" , order = 6)
    private String registrationDate;

    /**
     * 发动机编号
     */
    @ExcelProperty(value = "发动机号" , order = 7)
    private String engineNumber;
    /**
     * 排放标准
     */
    @ExcelProperty(value = "排放等级" , order = 8)
    private String emissionStandard;

    /**
     * 行驶证路径
     */
    @ExcelProperty(value = "行驶证" , order = 13)
    private String drivingLicense;

    /**
     * 随车清单路径
     */
    @ExcelProperty(value = "车辆图片" , order = 14)
    private String followInventory;

    /**
     * 单位
     */
    @ExcelProperty(value = "单位" , order = 11)
    private String unit;

    /**
     * 组织名称
     */
    @ExcelProperty(value = "组织名称" , order = 15)
    private String orgName = "四川华西绿舍精城建材有限公司";
    /**
     * 组织编码
     */
    @ExcelProperty(value = "组织编码" , order = 16)
    private String orgCode = "HXJCJC";

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间" , order = 17)
    private String creatTime;



}
