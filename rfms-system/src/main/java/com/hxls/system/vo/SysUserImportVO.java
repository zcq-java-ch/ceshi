package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户导入vo
 *
 * @author
 *
 */
@Data
public class SysUserImportVO  implements Serializable{
    private static final long serialVersionUID = 1L;

    @Schema(description = "机构ID")
    private Long orgId;


    @Schema(description = "导入路径")
    @NotBlank(message = "导入路径不能为空")
    private String imageUrl;




}
