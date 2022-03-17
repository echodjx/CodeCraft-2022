package com.huawei.java.algorithms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class BaseData {
    // 线上
    public static final String FILE_PATH = "/data/";
    public static final String OUTPUT_PATH = "/output/";

    // 测试
//    public static final String FILE_PATH = "src//com//huawei//java//data//";
//    public static final String OUTPUT_PATH = "src//com//huawei//java//data//output//";
//    public static final String FILE_PATH = "../data/";
//    public static final String OUTPUT_PATH = "../output/";
    // <边缘节点名,边缘节点带宽>
    public static HashMap<String, Integer> bandWidth;
    // 时间--> < 客户名 , 需求 >
    public static LinkedHashMap<String, Map<String, Integer>> demand;

    // 客户名-> <边缘节点,延迟>
    public static HashMap<String, Map<String, Integer>> QoS;

    // Config
    public static Integer qos_constraint;

    public static Integer edgeNum;
    public static Integer customerNum;

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

    public BaseData() {
        readBW();
        readDemand();
        readQos();
        readConfig();
    }
}
