package com.hxls.appointment.pojo.entity;

import java.io.Serializable;
import java.util.Date;


import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

/**
 * 预约补录表
 * @TableName t_supplement_record
 */
@TableName("t_supplement_record")
@Data
@EqualsAndHashCode(callSuper=false)
public class TSupplementRecord extends BaseEntity {

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 对应站点id
     */
    private Long siteId;

    /**
     * 出入类型（数据字典）
     */
    private String accessType;

    /**
     * 出入通道
     */
    private String channel;

    /**
     * 补录类型（数据字典）
     */
    private String supplementType;

    /**
     * 补录时间
     */
    private Date supplementTime;

    /**
     * 表单信息
     */
    private Object remark1;

}