package com.hxls.appointment.pojo.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TIssueEigenvalueVO {

    private List<String> succeed ;

    private List<String> fail;

    /**
     * 下发站点名称
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
     * 状态  0：失败   1：下发中  2 正常
     */
    private Integer status = 0 ;

    private Long id;

    private Long creator;

    private Date createTime;

    private Long  updater;

    private Date updateTime;

    private Integer version;

    private Integer deleted;

    private String name;

    private String carNumber;

}
