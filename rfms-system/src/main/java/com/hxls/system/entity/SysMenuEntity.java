package com.hxls.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * 菜单管理
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_menu")
public class SysMenuEntity extends BaseEntity {
    /**
     * 上级ID
     */
    private Long pid;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单URL
     */
    private String url;
    /**
     * 授权标识(多个用逗号分隔，如：sys:menu:list,sys:menu:save)
     */
    private String authority;
    /**
     * 类别（1、web端   2、移动端）
     */
    private Integer category;
    /**
     * 类型   0：菜单   1：按钮   2：接口
     */
    private Integer type;
    /**
     * 打开方式   0：内部   1：外部
     */
    private Integer openStyle;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 排序
     */
    private Integer sort;

    @TableField(exist = false)
    private Long ppid;

}
