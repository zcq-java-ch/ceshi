package com.hxls.framework.common.utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构工具类，如：菜单、机构等
 *
 * @author
 *
 */
public class TreeUtils {

    /**
     * 根据pid，构建树节点
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> treeNodes, Long pid) {
        // pid不能为空
        AssertUtils.isNull(pid, "pid");

        List<T> treeList = new ArrayList<>();
        for (T treeNode : treeNodes) {
            if (pid.equals(treeNode.getPid())) {
                treeList.add(findChildren(treeNodes, treeNode));
            }
        }

        return treeList;
    }

    /**
     * 查找子节点
     */
    private static <T extends TreeNode<T>> T findChildren(List<T> treeNodes, T rootNode) {
        for (T treeNode : treeNodes) {
            if (rootNode.getId().equals(treeNode.getPid())) {
                rootNode.getChildren().add(findChildren(treeNodes, treeNode));
            }
        }
        return rootNode;
    }

    /**
     * 构建树节点
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> treeNodes) {
        List<T> result = new ArrayList<>();

        // list转map
        Map<Long, T> nodeMap = new LinkedHashMap<>(treeNodes.size());
        for (T treeNode : treeNodes) {
            nodeMap.put(treeNode.getId(), treeNode);
        }

        for (T node : nodeMap.values()) {
            // 设置当前节点的ppid
            setPpidRecursively(node, nodeMap);
            result.add(node);

        }

        return result;
    }


    /**
     * 递归设置节点的ppid
     */
    private static <T extends TreeNode<T>> void setPpidRecursively(T node, Map<Long, T> nodeMap) {
        if (node.getPid() != null) {
            T parent = nodeMap.get(node.getPid());
            if (parent != null) {
                // 设置当前节点的ppid为其父节点的id
                node.setPpid("p"+parent.getId());
                // 如果父节点有ppid，则设置当前节点的ppid为其父节点的ppid
                if (parent.getPpid() != null && parent.getPpid().length() > 1) {
                    node.setPpid(parent.getPpid());
                }
            } else {
                // 如果找不到父节点，设置ppid为null
                node.setPpid(null);
            }
        }
    }

}
