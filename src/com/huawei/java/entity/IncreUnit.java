package com.huawei.java.entity;

public class IncreUnit implements Comparable<IncreUnit>{

    public String name;
    public Integer cost;

    public IncreUnit(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    @Override
    public int compareTo(IncreUnit unit) {
        return this.cost - unit.cost;
    }
}
