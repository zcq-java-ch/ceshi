package com.hxls.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.mybatis.entity.BaseEntity;
import com.hxls.system.enums.DataScopeEnum;

/**
 * 角色
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity {
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色编码
     */
    private String roleCode;
    /**
     * 备注
     */
    private String remark;
    /**
     * 数据范围  {@link DataScopeEnum}
     */
    private Integer dataScope;
    /**
     * 机构ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 状态 0:停用, 1:启用
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer sort;
}
