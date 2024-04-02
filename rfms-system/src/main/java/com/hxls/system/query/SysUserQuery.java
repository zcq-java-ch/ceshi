package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;

/**
 * 用户查询
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户查询")
public class SysUserQuery extends Query {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "机构ID")
    private Long orgId;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "代班负责人")
    private String supervisor;

    @Schema(description = "车牌")
    private String licensePlate;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "用户类型")
    private Integer status;

}
