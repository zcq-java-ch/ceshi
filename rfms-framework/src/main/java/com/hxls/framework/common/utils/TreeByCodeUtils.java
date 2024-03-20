package com.hxls.framework.common.utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构工具类，如：组织机构
 *
 * @author
 *
 */
public class TreeByCodeUtils {

    /**
     * 根据pcode，构建树节点
     */
    public static <T extends TreeNodeByCode<T>> List<T> build(List<T> treeNodes, String pcode) {
        // pcode不能为空
        AssertUtils.isNull(pcode, "pcode");

        List<T> treeList = new ArrayList<>();
        for (T treeNode : treeNodes) {
            if (pcode.equals(treeNode.getPcode())) {
                treeList.add(findChildren(treeNodes, treeNode));
            }
        }

        return treeList;
    }

    /**
     * 查找子节点
     */
    private static <T extends TreeNodeByCode<T>> T findChildren(List<T> treeNodes, T rootNode) {
        for (T treeNode : treeNodes) {
            if (rootNode.getCode().equals(treeNode.getPcode())) {
                rootNode.getChildren().add(findChildren(treeNodes, treeNode));
            }
        }
        return rootNode;
    }

    /**
     * 构建树节点
     */
    public static <T extends TreeNodeByCode<T>> List<T> build(List<T> treeNodes) {
        List<T> result = new ArrayList<>();

        // list转map
        Map<String, T> nodeMap = new LinkedHashMap<>(treeNodes.size());
        for (T treeNode : treeNodes) {
            nodeMap.put(treeNode.getCode(), treeNode);
        }

        for (T node : nodeMap.values()) {
            T parent = nodeMap.get(node.getPcode());
            if (parent != null && !(node.getCode().equals(parent.getCode()))) {
                parent.getChildren().add(node);
                continue;
            }

            result.add(node);
        }

        return result;
    }

}
