package com.huawei.java.entity;

public class Edges {

    // 边缘节点名称
    public String[] names;

    // 边缘节点最大带宽
    public Integer[] maxWidths;

    // 边缘节点剩余带宽
    public Integer[] leftWidths;

    public Edges (int n) {
        this.names = new String[n];
        this.maxWidths = new Integer[n];
        this.leftWidths = new Integer[n];
    }
    public Edges () {
    }
}
