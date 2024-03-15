package com.hxls.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.query.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* banner管理查询
*
* @author zhaohong 
* @since 1.0.0 2024-03-13
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "banner管理查询")
public class TBannerQuery extends Query {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "状态 0:停用, 1:启用")
    private Integer status;

}