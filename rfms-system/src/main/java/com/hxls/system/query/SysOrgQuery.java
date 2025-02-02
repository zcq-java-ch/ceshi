package com.hxls.system.query;

import com.hxls.framework.common.query.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 组织查询
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "组织查询")
public class SysOrgQuery extends Query {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "组织编码")
    private String code;

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "上级组织编码")
    private String pcode;

    @Schema(description = "类型")
    private Integer property;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

    private List<Long> orgList;

    private Long creator;

}
