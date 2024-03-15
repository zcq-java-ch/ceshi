package com.hxls.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.hxls.framework.common.utils.DateUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * AccessToken
 *
 * @author
 *
 */
@Data
@Schema(description = "AccessToken")
public class AccessTokenVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "access_token")
    @JsonProperty(value = "access_token")
    private String accessToken;

    @Schema(description = "access_token 过期时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date accessTokenExpire;

}
