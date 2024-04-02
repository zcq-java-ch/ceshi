package com.hxls.framework.common.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 树节点，所有需要实现树节点的，都需要继承该类
 *
 * @author
 *
 */
@Data
public class TreeNode<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Schema(description = "id")
    private Long id;
    /**
     * 上级ID
     */
    @Schema(description = "上级ID")
    private Long pid;

    /**
     * 最上级ID
     */
//    @Schema(description = "最上级ID")
//    private String ppid;
    /**
     * 子节点列表
     */
    private List<T> children = new ArrayList<>();
}
