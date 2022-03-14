package com.huawei.java.main;

import com.huawei.java.entity.Customers;
import com.huawei.java.entity.Edges;
import com.huawei.java.util.PreprocessData;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Main {

    public static Map<String, Map<String, Integer>> QosMap;

    public static void main(String[] args) {
        // 读入工具类
        PreprocessData pd = new PreprocessData();
        Map<String, Customers> timeLine = new HashMap<>();
        Edges edges =new Edges();
        QosMap = new HashMap<>();

        pd.readCustomerData(timeLine);
        pd.readEdgesData(edges);
        pd.readQoSData(QosMap);


        System.out.println(QosMap);
        System.out.println(edges);
        System.out.println(timeLine);

    }
}
