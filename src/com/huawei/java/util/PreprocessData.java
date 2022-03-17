package com.huawei.java.util;

import com.huawei.java.entity.Customers;
import com.huawei.java.entity.Edges;
import com.huawei.java.entity.QosLimit;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Properties;
public class PreprocessData {
    private static final String FILE_PATH = "src//com//huawei//java//data//";

    public static void readCustomerData(Map<String, Customers> timeLine) {
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH + "demand.csv"));
            // 数据项
            line = reader.readLine();
            String[] dataItem = line.split(",");
            String[] names = new String[dataItem.length - 1];
            for (int i = 0; i < names.length; i++) {
                names[i] = dataItem[i + 1];
            }
            int n = dataItem.length - 1;
            // 时间序列
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                Customers customers = new Customers(n);
                customers.names = Arrays.copyOf(names, n);
                for (int i = 0; i < temp.length - 1; i++) {
                    customers.needWidths[i] = Integer.parseInt(temp[i + 1]);
                }
                timeLine.put(temp[0], customers);
//                System.out.println(line);
            }
            System.out.println("客户信息读取完成...");
//            return true
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

    public static void readEdgesData(Edges edges) {
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH + "site_bandwidth.csv"));
            // 数据项
            reader.readLine();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<Integer> maxWidths = new ArrayList<>();


            // 时间序列
            while ((line = reader.readLine()) != null) {
                String[] item = line.split(",");
                names.add(item[0]);
                maxWidths.add(Integer.parseInt(item[1]));
            }
            int n = names.size();
            edges.names = names.toArray(new String[n]);
            edges.maxWidths = maxWidths.toArray(new Integer[n]);
            edges.leftWidths = maxWidths.toArray(new Integer[n]);
//            return true
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

    public static void readQoSData(Map<String, Map<String, Integer>> QosMap) {
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH + "qos.csv"));
            // 数据项
            line = reader.readLine();
            String[] dataItem = line.split(",");
            String[] names = Arrays.copyOfRange(dataItem, 1, dataItem.length);
            HashMap<Integer, String> hashMap = new HashMap<>();
            int n = dataItem.length - 1;
            for (int i = 0; i < n; i++) {
                hashMap.put(i, names[i]);
            }

            // 时间序列
            while ((line = reader.readLine()) != null) {
                String[] item = line.split(",");
                Map<String, Integer> map = new HashMap<>();
                for (int i = 1; i < item.length; i++) {
                    map.put(hashMap.get(i - 1), Integer.parseInt(item[i]));
                }

                QosMap.put(item[0], map);
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

    public static void readConfig() throws IOException {
        //创建文件输入流
        FileInputStream fis = new FileInputStream(FILE_PATH + "config.ini");
        Properties pps = new Properties();
        //从文件流中加载属性
        pps.load(fis);
        QosLimit.qos_constraint = Integer.parseInt(pps.getProperty("qos_constraint"));
        System.out.println("QoS限制读取完成:"+QosLimit.qos_constraint);
    }
}
