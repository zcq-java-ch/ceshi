package com.hxls.system.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @TableName sys_online_log
 */
@TableName("sys_online_log")
@Data
@EqualsAndHashCode(callSuper=false)
public class SysOnlineLog  extends BaseEntity implements Serializable {


    /**
     * 厂站id
     */

    private Long siteId;

    /**
     * 主机ip
     */

    private String masterIp;

    /**
     * 离线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime offDate;

    /**
     * 在线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime onDate;


    private static final long serialVersionUID = 1L;
}