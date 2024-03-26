package com.hxls.system.vo;

import com.hxls.framework.common.utils.TreeNodeByCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机构列表
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "组织")
public class SysOrgVO extends TreeNodeByCode<SysOrgVO> {


    @Schema(description = "单位类型（1：公司 2：部门）")
    private Integer property;

    @Schema(description = "组织编码")
    private String code;

    @Schema(description = "上级组织编码")
    private String pcode;

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "上级组织编码名称")
    private String pname;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "图标")
    private String orgIcon;

    @Schema(description = "是否是虚拟组织")
    private Integer virtualFlag;

    @Schema(description = "简称")
    private String orgAlias;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

}
