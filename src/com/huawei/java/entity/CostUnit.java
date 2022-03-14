package com.huawei.java.entity;

// 消耗的带宽单元，记录消耗边缘节点和带宽量
public class CostUnit {

    // 消耗节点名
    public String name;

    // 消耗节点带宽数量
    public Integer count;

    public CostUnit(String name, int count) {
        this.name = name;
        this.count = count;
    }
}
