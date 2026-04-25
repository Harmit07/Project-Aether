package com.aether.simulator.model;


import java.util.*;

public class Edge {
    private Node source;
    private Node destination;
    private Telemetry telemetry;

    public Edge(Node source,Node destination)
    {
        this.source=source;
        this.destination=destination;

        this.telemetry=new Telemetry(20,-60);

    }

    public Node getDestination() {
        return destination;
    }

    public Node getSource() {
        return source;
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }
}
