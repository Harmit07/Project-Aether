package com.aether.simulator.model;

public class TelemetryEvent {
    private long timestamp;
    private String sourceNode;
    private String destinationNode;
    private int latencyMs;
    private int signalStrengthDbm;

    public TelemetryEvent(long timestamp,String sourceNode,String destinationNode,int latencyMs,int signalStrengthDbm)
    {
        this.timestamp=timestamp;
        this.sourceNode=sourceNode;
        this.destinationNode=destinationNode;
        this.latencyMs=latencyMs;
        this.signalStrengthDbm=signalStrengthDbm;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public int getSignalStrengthDbm() {
        return signalStrengthDbm;
    }

    public int getLatencyMs() {
        return latencyMs;
    }
}
