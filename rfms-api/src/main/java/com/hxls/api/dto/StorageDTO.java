package com.hxls.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传
 *
 * @author
 *
 */
@Data
@Tag(name="文件上传")
public class StorageDTO implements Serializable {
    @Schema(description = "URL")
    private String url;
    @Schema(description = "文件大小")
    private Long size;

}
