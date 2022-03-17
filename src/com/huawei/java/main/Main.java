package com.huawei.java.main;

import com.huawei.java.entity.Customers;
import com.huawei.java.entity.Edges;
import com.huawei.java.entity.Node;
import com.huawei.java.util.PreprocessData;

import java.io.*;
import java.util.*;

public class Main {

    // 线上
//    private static final String FILE_PATH = "/data/";
//    public static final String OUTPUT_PATH = "/output/";

    // 测试
    private static final String FILE_PATH = "src//com//huawei//java//data//";
    public static final String OUTPUT_PATH = "src//com//huawei//java//data//output//";

    // <边缘节点名,边缘节点带宽>
    private static HashMap<String, Integer> bandWidth;
    // 时间--> < 客户名 , 需求 >
    private static LinkedHashMap<String, Map<String, Integer>> demand;

    // 客户名-> <边缘节点,延迟>
    private static HashMap<String, Map<String, Integer>> QoS;

    // Config
    private static Integer qos_constraint;

    private static Integer edgeNum;
    private static Integer customerNum;


    public static void main(String[] args) {
//        System.out.println(Main.class.getClassLoader().getResource("").getPath());
        readBW();
        readDemand();
        readQos();
        readConfig();
        greedyForMinQos();
    }

    public static void greedyForMinQos() {

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

                while (demandUnit.getValue() > 0) {
                    Node node = heap.poll();
                    Integer allocate = Math.min(demandUnit.getValue(), bandWidthCopy.get(node.name));
                    if (allocate != 0) {
                        demandUnit.setValue(demandUnit.getValue() - allocate);
                        bandWidthCopy.put(node.name, bandWidthCopy.get(node.name) - allocate);
                        if (resMap.get(demandUnit.getKey()) == null)
                            resMap.put(demandUnit.getKey(), new TreeMap<>());
                        resMap.get(demandUnit.getKey()).put(node.name, allocate);
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

    public static void greedy() {

        // 每个时间片单独处理
        // 每个时间片 , 每个客户有一个单独的 可连接节点的列表
        // 每个客户一个
        // 暂时不考虑客户优先级

        for (Map.Entry<String, Map<String, Integer>> time : demand.entrySet()) {
            LinkedHashMap<String, HashMap<String, Integer>> ans = new LinkedHashMap<>();
            System.out.println();
            System.out.println("时刻 : " + time.getKey());
            Map<String, Integer> demandSlice = time.getValue();
            for (Map.Entry<String, Integer> customer : demandSlice.entrySet()) {
                String name = customer.getKey();
                Integer demandVal = customer.getValue();

                ArrayList<String> connectList = new ArrayList<>();
                // 时延要求
                Map<String, Integer> QoSList = QoS.get(name);

                for (Map.Entry<String, Integer> QoSen : QoSList.entrySet()) {
                    String edgeName = QoSen.getKey();
                    Integer edgeQos = QoSen.getValue();
                    if (edgeQos < qos_constraint) {
                        connectList.add(edgeName);
                    }
                }

                // 时延排序
//                connectList.sort(new Comparator<String>() {
//                    @Override
//                    public int compare(String o1, String o2) {
//                        return 0;
//                    }
//                });

                // 流量分配
                // 剩余流量列表

                HashMap<String, Integer> remainBW = new HashMap<>();
                for (Map.Entry<String, Integer> bw : bandWidth.entrySet()) {
                    remainBW.put(bw.getKey(), bw.getValue());
                }
                // 列表节点 全部分配

                // 分配结果
                HashMap<String, Integer> ansMap = new HashMap<>();
                for (int i = 0; i < connectList.size(); i++) {
                    String nodeName = connectList.get(i);
                    if (demandVal == 0) break;
                    if (remainBW.get(nodeName) >= demandVal) {
                        ansMap.put(nodeName, demandVal);
                        remainBW.put(nodeName, remainBW.get(nodeName) - demandVal);
                        demandVal = 0;
                    } else {
                        ansMap.put(nodeName, remainBW.get(nodeName));
                        remainBW.put(nodeName, 0);
                        demandVal = demandVal - remainBW.get(nodeName);
                    }

                }
                ans.put(name, ansMap);
//                System.out.print("客户-" + name + "-");
//                for (Map.Entry<String, Integer> entry : ansMap.entrySet()) {
//                    System.out.print(entry.getKey() + " : " + entry.getValue() + "||");
//                }

            }
            // 写入文件
            BufferedWriter buffw = null;
            try {
                buffw = new BufferedWriter(new FileWriter( "/output/solution.txt",
                        true));
                for (Map.Entry<String, HashMap<String, Integer>> an : ans.entrySet()) {
                    String temp = an.getKey() + ":";
                    if (an.getValue().size() == 0) {
                        buffw.append(temp + "\n");
                        continue;
                    }
                    for (Map.Entry<String, Integer> en : an.getValue().entrySet()) {
                        temp = temp + "<" + en.getKey() + "," + en.getValue() + ">" + ",";
                    }
                    temp = temp.substring(0, temp.length() - 1);
                    buffw.append(temp + "\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (buffw != null) buffw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        System.out.println();
    }

    public static void readConfig() {
        qos_constraint = 0;
        FileInputStream fis = null;
        try {
            //创建文件输入流
            fis = new FileInputStream(FILE_PATH + "config.ini");
            Properties pps = new Properties();
            //从文件流中加载属性
            pps.load(fis);
            qos_constraint = Integer.parseInt(pps.getProperty("qos_constraint"));
            System.out.println("QoS限制读取完成:" + qos_constraint);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public static void readQos() {
        QoS = new HashMap<>();
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH + "qos.csv"));
            // 数据项
            line = reader.readLine();
            String[] dataItem = line.split(",");
            int n = dataItem.length - 1;
            // 时间序列
            while ((line = reader.readLine()) != null) {
                String[] item = line.split(",");

                for (int i = 1; i < item.length; i++) {
                    if (QoS.containsKey(dataItem[i])) {
                        Map<String, Integer> slice = QoS.get(dataItem[i]);
                        slice.put(item[0], Integer.parseInt(item[i]));
                    } else {
                        Map<String, Integer> slice = new HashMap<>();
                        slice.put(item[0], Integer.parseInt(item[i]));
                        QoS.put(dataItem[i], slice);
                    }
                }
            }
            System.out.println("QoS信息读取完成...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readDemand() {
        demand = new LinkedHashMap<>();

        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH + "demand.csv"));
            // 数据项
            line = reader.readLine();
            String[] dataItem = line.split(",");
            int n = dataItem.length - 1;

            // 时间序列
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                Map<String, Integer> item = new HashMap<>();
                for (int i = 1; i < temp.length; i++) {
                    item.put(dataItem[i], Integer.parseInt(temp[i]));
                    demand.put(temp[0], item);
                }
                customerNum = n;
            }
            System.out.println("客户信息读取完成...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void readBW() {
        bandWidth = new HashMap<>();
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH+"site_bandwidth.csv"));
            // 数据项
            reader.readLine();
            int n = 0;
            while ((line = reader.readLine()) != null) {
                String[] item = line.split(",");
                bandWidth.put(item[0], Integer.parseInt(item[1]));
                n++;
            }
            edgeNum = n;
            System.out.println("边缘节点信息读取完成...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
