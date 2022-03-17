package com.huawei.java.algorithms;

import com.huawei.java.entity.Node;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class AvgAlgorithm extends BaseDataOffline {

    public void greedyForAvgWidth() {

        BufferedWriter bufw = null;
        try {
            bufw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH + "solution.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 遍历时间片
        for (Map<String, Integer> demands : demand.values()) {
            HashMap<String, Integer> bandWidthCopy = new HashMap<>(bandWidth);

            // 输出Map
            Map<String, Map<String, Integer>> resMap = new HashMap<>();
            // 遍历客户需求
            for (Map.Entry<String, Integer> demandUnit : demands.entrySet()) {
                PriorityQueue<Node> heap = new PriorityQueue<>();

                // 遍历当前客户对各边缘节点QoS
                for (Map.Entry<String, Integer> qosUnit : QoS.get(demandUnit.getKey()).entrySet()) {
                    heap.add(new Node(qosUnit.getKey(), qosUnit.getValue()));
                }

                // 删去不符合条件节点
                while (heap.peek().qos >= qos_constraint) {
                    heap.poll();
                }

                Integer avg = demandUnit.getValue() / heap.size() + 1;
                PriorityQueue<Node> tmpQueue = new PriorityQueue<>();
                while (demandUnit.getValue() > 0) {
                    Node node = heap.poll();
                    Integer allocate = Math.min(avg, bandWidthCopy.get(node.name));
                    allocate = Math.min(allocate, demandUnit.getValue());
                    if (allocate != 0) {
                        demandUnit.setValue(demandUnit.getValue() - allocate);
                        bandWidthCopy.put(node.name, bandWidthCopy.get(node.name) - allocate);
                        if (resMap.get(demandUnit.getKey()) == null)
                            resMap.put(demandUnit.getKey(), new TreeMap<>());
                        resMap.get(demandUnit.getKey()).put(node.name, resMap.get(demandUnit.getKey()).getOrDefault(node.name, 0) + allocate);
                    }
                    if (bandWidthCopy.get(node.name) != 0)
                        tmpQueue.add(node);
                    if (heap.size() == 0) {
                        while (tmpQueue.size() != 0)
                            heap.add(tmpQueue.poll());
                        avg = demandUnit.getValue() / heap.size() + 1;
                    }
                }

                if (resMap.get(demandUnit.getKey()) == null) {
                    resMap.put(demandUnit.getKey(), new TreeMap<>());
                }

            }
            // 输出

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

        try {
            bufw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
