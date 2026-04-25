package com.aether.simulator.model;

public class Telemetry {

    private int latencyMs;
    private int signalStrengthDbm;


    public Telemetry(int latencyMs,int signalStrengthDbm)
    {
        this.latencyMs=latencyMs;
        this.signalStrengthDbm=signalStrengthDbm;
    }

    public void updateMetrics(int latencyMs,int signalStrengthDbm)
    {
        this.latencyMs=latencyMs;
        this.signalStrengthDbm=signalStrengthDbm;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public int getSignalStrengthDbm() {
        return signalStrengthDbm;
    }
}
