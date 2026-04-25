package com.aether.simulator.model;

import java.util.Objects;

public class Node {

    private String nodeId;
    private String deviceType;

    public Node(String nodeId,String deviceType)
    {
        this.nodeId=nodeId;
        this.deviceType=deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this==o)return true;
        if(o==null || getClass()!=o.getClass())return false;
        Node node=(Node)o;
        return Objects.equals(nodeId,node.nodeId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nodeId);
    }
}
