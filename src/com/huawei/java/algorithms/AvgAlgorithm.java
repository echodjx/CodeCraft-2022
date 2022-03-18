package com.huawei.java.algorithms;

import com.huawei.java.entity.Node;
import com.huawei.java.util.OutputUtil;

import java.io.*;
import java.util.*;

// 平均分配每个客户节点的需求到各边缘节点  1374406
public class AvgAlgorithm extends BaseDataOffline {

    public void greedyForAvgWidth() {
        OutputUtil.loadWriter(OUTPUT_PATH + "solution.txt");
        BufferedWriter bufw = null;
        try {
            bufw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH + "solution.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //寻找负载节点
        HashMap<String, Integer> loadSort = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> Edges : QoS.entrySet()) {
            for (Map.Entry<String, Integer> EtoC : Edges.getValue().entrySet()) {
                if(EtoC.getValue() < qos_constraint) {
                    if(loadSort.get(EtoC.getKey()) == null) {
                        loadSort.put(EtoC.getKey(),new Integer(0));
                    }
                    loadSort.put(EtoC.getKey(),loadSort.get(EtoC.getKey()) + 1);
                }
            }
        }
        Comparator<Node> LengthComparator = new Comparator<Node>() {
            public int compare(Node s1, Node s2) {
                return s2.qos - s1.qos;//这里是边缘节点频次 不是qos
            }
        };
        PriorityQueue<Node> loadList = new PriorityQueue<>(LengthComparator);
        for (Map.Entry<String, Integer> enter : loadSort.entrySet()) {
            loadList.add(new Node(enter.getKey(),enter.getValue()));
        }
        int rdnum = bandWidth.size()/30;//负载节点数量
        HashMap<String, Integer> loadNode = new HashMap<>();//建立负载节点
        while(!loadList.isEmpty()){
            String str = loadList.poll().name;
            loadNode.put(str,1);
            rdnum--;
            if(rdnum <= 0) break;
        }

        // 遍历时间片
        for (Map<String, Integer> demands : demand.values()) {
            HashMap<String, Integer> bandWidthCopy = new HashMap<>(bandWidth);

            // 输出Map
            Map<String, Map<String, Integer>> resMap = new HashMap<>();
            // 遍历客户需求
            for (Map.Entry<String, Integer> demandUnit : demands.entrySet()) {
                PriorityQueue<Node> heap = new PriorityQueue<>();
                if (demandUnit.getValue() == 0) {
                    if (resMap.get(demandUnit.getKey()) == null)
                        resMap.put(demandUnit.getKey(), new TreeMap<>());
                    continue;
                }
                boolean flag = true;
                // 遍历当前客户对各边缘节点QoS
                for (Map.Entry<String, Integer> qosUnit : QoS.get(demandUnit.getKey()).entrySet()) {
                    if (demandUnit.getValue() == 0) {
                        flag = false;
                        break;
                    }
                    if (qosUnit.getValue() < qos_constraint && loadNode.containsKey(qosUnit.getKey()) && bandWidthCopy.get(qosUnit.getKey()) > 0) {
                        if (resMap.get(demandUnit.getKey()) == null)
                            resMap.put(demandUnit.getKey(), new TreeMap<>());
                        if (bandWidthCopy.get(qosUnit.getKey()) >= demandUnit.getValue()) {
                            resMap.get(demandUnit.getKey()).put(qosUnit.getKey(), demandUnit.getValue());
                            bandWidthCopy.put(qosUnit.getKey(), bandWidthCopy.get(qosUnit.getKey()) - demandUnit.getValue());
                            demandUnit.setValue(0);
                            flag = false;
                        } else {
                            resMap.get(demandUnit.getKey()).put(qosUnit.getKey(), bandWidthCopy.get(qosUnit.getKey()));
                            demandUnit.setValue(demandUnit.getValue() - bandWidthCopy.get(qosUnit.getKey()));
                            bandWidthCopy.put(qosUnit.getKey(), 0);
                        }
                    }
                    heap.add(new Node(qosUnit.getKey(), qosUnit.getValue()));
                }
                if (flag) {
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
            }
            // StepMinimizer优化
            StepMinimizer minimizer = new StepMinimizer();
            minimizer.start(StepMinimizer.MINIMIZER_TYPE, resMap);
            OutputUtil.printWidthCost(resMap);
            OutputUtil.writeToSolution(resMap);
        }
    }
}
