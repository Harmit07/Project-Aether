package com.aether.controller.model;

import java.util.List;

public class NetworkModels {

    public record Node(String id,String type){}
    public record Edge(String sourceId, String destinationId, int currentLatency){}
    public record TopologyState(List<Node> nodes, List<Edge> edges){}
    public record AnomalyAlert(long timestamp, String sourceNode,String destinationNode,int latencyMs,int signalStrengthDbm,String status){}

    public record RemediationPolicy(
            String targetNode,
            String action,
            int newEnforcedLatency,
            String humanReadableJustification
    ) {}
}
