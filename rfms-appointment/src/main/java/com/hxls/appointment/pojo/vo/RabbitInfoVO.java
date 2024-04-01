package com.hxls.appointment.pojo.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息表
 * @TableName info
 */
@Data
public class RabbitInfoVO implements Serializable {

    /**
     * 访问ip
     */
    private String ip ;

    /**
     * 指令id
     */
    private String instructionId;

    /**
     * 指令类型 1：人脸 ； 2：车辆
     */
    private Integer instructionType;

    /**
     * 厂家类型
     */
    private String type;

    /**
     * 访客名字
     */
    private String visitorName;

    /**
     * 生物特征路径
     */
    private String faceUrl;

    /**
     * 访客编码
     */
    private String visitorCode;

    /**
     * 车牌号
     */
    private String carPlateNumber;


    /**
     * 设备信息
     */
    private List<DeviceInfo> deviceInfos;


}
