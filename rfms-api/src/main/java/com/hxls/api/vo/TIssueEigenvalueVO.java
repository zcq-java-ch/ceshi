package com.hxls.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TIssueEigenvalueVO {

    private List<String> succeed ;

    private List<String> fail;

    /**
     * 授权厂站
     */
    private String stationName;

    /**
     * 下发站点id
     */
    private Long stationId;

    /**
     * 下发区域名称
     */
    private String areaName;

    /**
     * 下发区域id
     */
    private Long areaId;

    /**
     * 下发设备名称
     */
    private String deviceName;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 下发类型（1为人员 ， 2为车辆）
     */
    private Integer type;

    /**
     * 下发数据
     */
    private String data;

    /**
     * 状态  0：停用   1：正常
     */
    private Integer status;


    private Long id;


    private Long  creator;
    private String  creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;


    private Long  updater;


    private Date updateTime;

    private Integer version;
    private Integer deleted;


    private String peopleName;
    private String peopleCode;
    private String faceUrl;

    private String carNumber;

    private String masterIp;

    private String masterName;

    private String time;

    private String postName;

    private String orgName;

}
