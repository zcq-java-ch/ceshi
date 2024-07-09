package com.hxls.system.vo;

import com.hxls.framework.common.utils.TreeNodeByCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

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

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "单位类型（1：公司 2：部门 3: 站点 来自字典）")
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


    /**
     * 站点地址（中文地址）
     * */
    @Schema(description = "站点地址（中文地址）")
    private String siteLocation;

    /**
     * 站点地址经度
     * */
    @Schema(description = "站点地址经度")
    private String siteLocationLon;

    /**
     * 站点地址纬度
     * */
    @Schema(description = "站点地址纬度")
    private String siteLocationLat;

    /**
     * 站点管理员(多个)
     * */
    @Schema(description = "站点管理员(多个)")
    private String siteAdminIds;

    /**
     * 站点管理员名字（多个）
     * */
    @Schema(description = "站点管理员名字（多个）")
    private String siteAdminNames;

    @Schema(description = "状态 0:正常, 1:删除")
    private Integer deleted;


    private List<SysSiteAreaVO> sysSiteAreaList;

    private String authOrgIdList;

    @Schema(description = "前端分级使用")
    private String areaId;

    @Schema(description ="是否管控 , 1是  0否")
    private Integer isControl;


}
