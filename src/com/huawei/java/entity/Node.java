package com.huawei.java.entity;

// 边缘节点即延迟中间类，用来排序
public class Node implements Comparable<Node>{

    // 边缘节点名
    public String name;

    // 边缘节点延迟
    public Integer qos;

    public Node (String name, Integer qos) {
        this.name = name;
        this.qos = qos;
    }

    @Override
    public int compareTo(Node node) {
        return node.qos - this.qos;
    }
}
