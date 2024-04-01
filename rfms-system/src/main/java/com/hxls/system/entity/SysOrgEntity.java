package com.hxls.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hxls.framework.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机构管理
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_org")
public class SysOrgEntity extends BaseEntity {

    /**
     * 单位类型（1：公司 2：部门 3: 站点 来自字典）
     */
    private Integer property;

    /**
     * 组织编码
     */
    private String code;

    /**
     * 上级组织编码
     */
    private String pcode;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 上级组织编码名称
     */
    private String pname;


    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String orgIcon;

    /**
     * 是否是虚拟组织 0否  1是
     */
    private Integer virtualFlag;

    /**
     * 简称
     */
    private String orgAlias;

    /**
     * 状态 0:停用, 1:启用
     */
    private Integer status;

    /**
     * 站点地址（中文地址）
     * */
    private String siteLocation;

    /**
     * 站点地址经度
     * */
    private String siteLocationLon;

    /**
     * 站点地址纬度
     * */
    private String siteLocationLat;

    /**
     * 站点管理员(多个)
     * */
    private String siteAdminIds;

    /**
     * 站点管理员名字（多个）
     * */
    private String siteAdminNames;
}
