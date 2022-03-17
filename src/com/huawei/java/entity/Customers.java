package com.huawei.java.entity;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 每个时间戳一个Customers对象包含所有客户的需求和当前满足
public class Customers {

    // 节点名
    public String[] names;

    // 需要的带宽量
    public Integer[] needWidths;

    // 当前满足带宽量的Map，存放客户节点Idx和消耗对应边缘节点的带宽，用来输出
    public Map<Integer, List<CostUnit>> costMap;

    public Customers (int n) {
        this.names = new String[n];
        this.needWidths = new Integer[n];
        costMap = new HashMap<>();
    }

}
