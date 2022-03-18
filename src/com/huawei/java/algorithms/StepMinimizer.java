package com.huawei.java.algorithms;

import com.huawei.java.entity.IncreUnit;
import com.huawei.java.util.OutputUtil;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StepMinimizer extends BaseDataOffline {

    // 客户节点对边缘节点映射
    public Map<String, Map<String, Integer>> cosEdgeMap;
    // 边缘节点对客户节点映射
    public Map<String, Map<String, Integer>> edgeCosMap;

    // 超参数，迭代次数
    public int EPOCHS = 30;
    // 超参数，递减初始值倍率
    public int INIT_MAG = 3;
    // 超参数，递减倍率
    public double DECRE_MAG = 0.9;

    // 初始化模式
    public final static int DEFAULT_TYPE = 0;
    public final static int MINIMIZER_TYPE = 1;

    public void defaultInit(Map<String, Integer> customerDemands) {
        cosEdgeMap = new HashMap<>();
        edgeCosMap = new HashMap<>();
        for (Map.Entry<String, Integer> demandUnit : customerDemands.entrySet()) {
            if (demandUnit.getValue() == 0) {
                cosEdgeMap.put(demandUnit.getKey(), new HashMap<>());
            }
            // 取出所有满足约束的边缘节点名
            List<String> fitEdgeNames = new ArrayList<>();
            for (Map.Entry<String, Integer> edgeQos : QoS.get(demandUnit.getKey()).entrySet()) {
                if (edgeQos.getValue() < qos_constraint)
                    fitEdgeNames.add(edgeQos.getKey());
            }

            // 初始化分配 递减策略->为保证step时0.95有下降防原地踏步
            Integer allocateBase = (demandUnit.getValue() / fitEdgeNames.size()) * INIT_MAG;
            for (String fitEdgeName : fitEdgeNames) {
                Integer allocate = Math.min(allocateBase, demandUnit.getValue());
                allocate = Math.min(allocate, bandWidth.get(fitEdgeName));
                if (allocate != 0) {
                    // 分配
                    demandUnit.setValue(demandUnit.getValue() - allocate);
                    bandWidth.put(fitEdgeName, bandWidth.get(fitEdgeName) - allocate);
                    Map<String, Integer> edgeUnit = cosEdgeMap.getOrDefault(demandUnit.getKey(), new HashMap<>());
                    edgeUnit.put(fitEdgeName, edgeUnit.getOrDefault(fitEdgeName, 0) + allocate);
                    cosEdgeMap.put(demandUnit.getKey(), edgeUnit);

                    Map<String, Integer> cosUnit = edgeCosMap.getOrDefault(fitEdgeName, new HashMap<>());
                    cosUnit.put(demandUnit.getKey(), cosUnit.getOrDefault(demandUnit.getKey(), 0) + allocate);
                    edgeCosMap.put(fitEdgeName, cosUnit);
                    allocateBase = (int) (allocateBase * DECRE_MAG);
                }
            }

            // 还不够的话接着分
            for (int i = 0; i < fitEdgeNames.size() && demandUnit.getValue() > 0; i++) {
                Integer allocate = Math.min(demandUnit.getValue(), bandWidth.get(fitEdgeNames.get(i)));
                if (allocate != 0) {
                    // 分配
                    demandUnit.setValue(demandUnit.getValue() - allocate);
                    bandWidth.put(fitEdgeNames.get(i), bandWidth.get(fitEdgeNames.get(i)) - allocate);

                    Map<String, Integer> edgeUnit = cosEdgeMap.getOrDefault(demandUnit.getKey(), new HashMap<>());
                    edgeUnit.put(fitEdgeNames.get(i), edgeUnit.getOrDefault(fitEdgeNames.get(i), 0) + allocate);
                    cosEdgeMap.put(demandUnit.getKey(), edgeUnit);

                    Map<String, Integer> cosUnit = edgeCosMap.getOrDefault(fitEdgeNames.get(i), new HashMap<>());
                    cosUnit.put(demandUnit.getKey(), cosUnit.getOrDefault(demandUnit.getKey(), 0) + allocate);
                    edgeCosMap.put(fitEdgeNames.get(i), cosUnit);
                }
            }
        }
    }

    public void minimizeInit(Map<String, Map<String, Integer>> resMap) {
        this.cosEdgeMap = resMap;
        this.edgeCosMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : resMap.entrySet()) {
            for (Map.Entry<String, Integer> edgeUnit : entry.getValue().entrySet()) {
                Map<String, Integer> childMap = edgeCosMap.getOrDefault(edgeUnit.getKey(), new HashMap<>());
                childMap.put(entry.getKey(), edgeUnit.getValue());
                edgeCosMap.put(edgeUnit.getKey(), childMap);
            }
        }
    }

    private void step() {
        for (int i = 0; i < EPOCHS; i++) {
            for (Map.Entry<String, Map<String, Integer>> edge : edgeCosMap.entrySet()) {
                List<IncreUnit> orderList = new ArrayList<>(edge.getValue().size());
                for (Map.Entry<String, Integer> cosUnit : edge.getValue().entrySet()) {
                    orderList.add(new IncreUnit(cosUnit.getKey(), cosUnit.getValue()));
                }
                Collections.sort(orderList);
                int idx = (int) Math.ceil(orderList.size() * 0.95) - 1;
                IncreUnit unit = orderList.get(idx);
                for (Map.Entry<String, Integer> edgeUnit : cosEdgeMap.get(unit.name).entrySet()) {
                    if (unit.cost == 1)
                        break;
                    if (edgeUnit.getKey().equals(edge.getKey()))
                        continue;
                    int L = 0, R = unit.cost;
                    while (L < R) {
                        int mid = L + ((R - L) >> 1);
                        if (tryAllocate(edgeUnit.getKey(), unit.name, mid)) {
                            L = mid + 1;
                        } else {
                            R = mid - 1;
                        }
                    }

                    L = Math.min(unit.cost - 1, L); // 起码留1点不然成0算的不对
                    if (L > 0) {
                        unit.cost -= L;
                        edgeCosMap.get(edge.getKey()).put(unit.name, edgeCosMap.get(edge.getKey()).get(unit.name) - L);
                        edgeCosMap.get(edgeUnit.getKey()).put(unit.name, edgeCosMap.get(edgeUnit.getKey()).get(unit.name) + L);
                    }
                }
                OutputUtil.printWidthCost(edgeCosMap);
            }
        }
    }

    private boolean tryAllocate(String edge2, String cos, Integer val) {
        List<IncreUnit> preList = new ArrayList<>();
        List<IncreUnit> afterList = new ArrayList<>();

        int band = 0;
        for (Map.Entry<String,Integer> cosUnit : edgeCosMap.get(edge2).entrySet()) {
            if (cosUnit.getKey().equals(cos)) {
                band += cosUnit.getValue() + val;
                afterList.add(new IncreUnit(cosUnit.getKey(), cosUnit.getValue() + val));
            } else {
                band += cosUnit.getValue();
                afterList.add(new IncreUnit(cosUnit.getKey(), cosUnit.getValue()));
            }
            preList.add(new IncreUnit(cosUnit.getKey(), cosUnit.getValue()));
        }

        Collections.sort(preList);
        Collections.sort(afterList);
        int idx = (int) Math.ceil(preList.size() * 0.95) - 1;
        if (preList.get(idx).cost >= afterList.get(idx).cost && band <= bandWidth.get(edge2))
            return true;
        return false;
    }


    public void start (Integer type, Map<String, Map<String, Integer>> resMap) {
        if (type == DEFAULT_TYPE) {
            OutputUtil.loadWriter(OUTPUT_PATH);
            for (Map.Entry<String, Map<String, Integer>> demands : demand.entrySet()) {
                // 首先需要init一个分配
                defaultInit(demands.getValue());
                // 开始迭代优化，思路取0.95位置的带宽看能不能给别人
                step();

                // 输出结果到solution
                OutputUtil.writeToSolution(cosEdgeMap);
            }
        } else if (type == MINIMIZER_TYPE) {
            minimizeInit(resMap);
            step();
        }
    }

    public void paramsChange(int epoch) {
        this.EPOCHS = epoch;
    }
}
