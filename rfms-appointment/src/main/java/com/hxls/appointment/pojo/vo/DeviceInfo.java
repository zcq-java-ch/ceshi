package com.hxls.appointment.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备信息表
 */
@Data
public class DeviceInfo implements Serializable {

    /**
     * ip号
     */
    private String ip;


    /**
     * 设备账号
     */
    private String username;

    /**
     * 设备密码
     */
    private String password;

    /**
     * 设备编号 -- 四川凌志恒需要
     */
    private String code;

    /**
     * 其他字段
     */
    private String mark;
}
