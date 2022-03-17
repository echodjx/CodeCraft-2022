package com.huawei.java.algorithms;

import com.huawei.java.entity.Node;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class AvgAlgorithm extends BaseData{

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
            int rdnum = bandWidthCopy.size()/20;//负载节点数量
            HashMap<String, Integer> loadNode = new HashMap<>();//建立负载节点
            for (Map.Entry<String, Integer> loadChose : bandWidthCopy.entrySet()) {
                rdnum--;
                loadNode.put(loadChose.getKey(),loadChose.getValue());
                if(rdnum <= 0) break;
            }
            // 输出Map
            Map<String, Map<String, Integer>> resMap = new HashMap<>();
            // 遍历客户需求
            for (Map.Entry<String, Integer> demandUnit : demands.entrySet()) {
                PriorityQueue<Node> heap = new PriorityQueue<>();
                if(demandUnit.getValue() == 0) {
                    if (resMap.get(demandUnit.getKey()) == null)
                        resMap.put(demandUnit.getKey(), new TreeMap<>());
                    continue;
                }
                boolean flag = true;
                // 遍历当前客户对各边缘节点QoS
                for (Map.Entry<String, Integer> qosUnit : QoS.get(demandUnit.getKey()).entrySet()) {
                    if(demandUnit.getValue() == 0) {
                        flag = false;
                        break;
                    }
                    if(qosUnit.getValue() < qos_constraint && loadNode.containsKey(qosUnit.getKey()) && bandWidthCopy.get(qosUnit.getKey()) > 0) {
                        if (resMap.get(demandUnit.getKey()) == null)
                            resMap.put(demandUnit.getKey(), new TreeMap<>());
                        if(bandWidthCopy.get(qosUnit.getKey()) >= demandUnit.getValue()) {
                            resMap.get(demandUnit.getKey()).put(qosUnit.getKey(),demandUnit.getValue());
                            bandWidthCopy.put(qosUnit.getKey(),bandWidthCopy.get(qosUnit.getKey()) -  demandUnit.getValue());
                            demandUnit.setValue(0);
                            flag = false;
                        }
                        else {
                            resMap.get(demandUnit.getKey()).put(qosUnit.getKey(),bandWidthCopy.get(qosUnit.getKey()));
                            demandUnit.setValue(demandUnit.getValue() - bandWidthCopy.get(qosUnit.getKey()));
                            bandWidthCopy.put(qosUnit.getKey(),0);
                        }
                    }
                    heap.add(new Node(qosUnit.getKey(), qosUnit.getValue()));
                }
                if (flag) {
                    // 删去不符合条件节点
                    while (heap.peek().qos >= qos_constraint ) {
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
