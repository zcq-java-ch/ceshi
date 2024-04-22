package com.hxls.appointment.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 预约车辆信息表
 * @TableName t_appointment_vehicle
 */
@Data
public class TAppointmentVehicleExcelVO implements Serializable {
    private static final long serialVersionUID = 1L;



    /**
     * 开始时间
     */
    @ExcelProperty("开始时间")
    private String startTime;

    /**
     * 开始时间
     */
    @ExcelProperty("开始时间")
    private String endTime;


    /**
     * 开始时间
     */
    @ExcelProperty("司机")
    private String passenger;

    /**
     * 车型（数据字典）
     */
    @ExcelProperty("车型")
    private String vehicleModel;



    /**
     * 车牌号
     */
    @ExcelProperty("车牌号")
    private String plateNumber;


    /**
     * 车辆排放标准（数据字典）
     */
    @ExcelProperty("车辆排放标准")
    private String emissionStandard;



}