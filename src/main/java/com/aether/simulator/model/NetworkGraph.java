package com.aether.simulator.model;


import java.util.*;
public class NetworkGraph {

    private Map<Node,List<Edge>> adjacencyList;

    public NetworkGraph()
    {
        this.adjacencyList=new HashMap<>();
    }
    public void addNode(Node node)
    {
        adjacencyList.putIfAbsent(node,new ArrayList<>());

    }

    public void addEdge(Node source, Node destination)
    {
        addNode(source);
        addNode(destination);
        adjacencyList.get(source).add(new Edge(source,destination));
    }

    public Map<Node,List<Edge>> getAdjacencyList()
    {
        return adjacencyList;
    }

}
