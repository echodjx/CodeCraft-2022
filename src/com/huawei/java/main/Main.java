package com.huawei.java.main;

import com.huawei.java.algorithms.AvgAlgorithm;
import com.huawei.java.algorithms.StepMinimizer;

public class Main {

    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
        AvgAlgorithm algorithm = new AvgAlgorithm();
        algorithm.greedyForAvgWidth();
//        long endTime = System.currentTimeMillis();
//        System.out.println("运行时间：" + ((endTime - startTime) / 1000.0) + " 秒");

    }

}
