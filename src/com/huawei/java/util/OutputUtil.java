package com.huawei.java.util;

import java.io.*;
import java.util.*;

public class OutputUtil {

    public static BufferedWriter bufw = null;

    public static void loadWriter(String fileName) {
        try {
            bufw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + "solution.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 输出到Solution
    public static void writeToSolution(Map<String, Map<String, Integer>> resMap) {
        try {
            for (Map.Entry<String, Map<String, Integer>> resEntry : resMap.entrySet()) {
                StringBuilder line = new StringBuilder();
                line.append(resEntry.getKey() + ":");
                for (Map.Entry<String,Integer> unit: resEntry.getValue().entrySet()) {
                    line.append("<" + unit.getKey() + "," + unit.getValue() + ">,");
                }
                if (line.charAt(line.length() - 1) == ',')
                    line.deleteCharAt(line.length() - 1);
                line.append("\n");
                bufw.write(line.toString());
                bufw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 打印loss
    public static void printWidthCost(Map<String, Map<String, Integer>> resMap) {
        long res = 0;
        Map<String, Map<String, Integer>> edgeCosMap = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : resMap.entrySet()) {
            for (Map.Entry<String, Integer> edgeUnit : entry.getValue().entrySet()) {
                Map<String, Integer> childMap = edgeCosMap.getOrDefault(edgeUnit.getKey(), new HashMap<>());
                childMap.put(entry.getKey(), edgeUnit.getValue());
                edgeCosMap.put(edgeUnit.getKey(), childMap);
            }
        }

        for (Map<String, Integer> cosUnit : edgeCosMap.values()) {
            List<Integer> orderList = new LinkedList<>();
            for (Integer val : cosUnit.values())
                orderList.add(val);
            Collections.sort(orderList);
            int idx = (int) (Math.ceil(orderList.size() * 0.95)) - 1;
            res += orderList.get(idx);
        }
        System.out.println("now 0.95 total width is: " + res);
    }
}
