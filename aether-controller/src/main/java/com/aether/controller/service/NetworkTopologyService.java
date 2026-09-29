package com.aether.controller.service;

import com.aether.controller.model.NetworkModels.Edge;
import com.aether.controller.model.NetworkModels.Node;
import com.aether.controller.model.NetworkModels.TopologyState;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NetworkTopologyService {

    private final List<Node> activeNodes = new ArrayList<>();
    private final List<Edge> activeEdges = new ArrayList<>();

    private final AtomicInteger latencyAB = new AtomicInteger(20);
    private final AtomicInteger latencyBD = new AtomicInteger(20);
    private final AtomicInteger latencyAC = new AtomicInteger(20);
    private final AtomicInteger latencyCD = new AtomicInteger(20);

    private final AtomicInteger latencyBE = new AtomicInteger(20);
    private final AtomicInteger latencyCF = new AtomicInteger(20);
    private final AtomicInteger latencyBG = new AtomicInteger(20);
    private final AtomicInteger latencyCH = new AtomicInteger(20);
    private final AtomicInteger latencyDI = new AtomicInteger(20);
    private final AtomicInteger latencyGI = new AtomicInteger(20);
    private final AtomicInteger latencyHI = new AtomicInteger(20);
    private final AtomicInteger latencyIJ = new AtomicInteger(20);

    public NetworkTopologyService(MeterRegistry meterRegistry) {

        String[] nodes = {"Tower_A", "Tower_B", "Tower_C", "Tower_D", "Tower_E", "Tower_F", "Tower_G", "Tower_H", "Tower_I", "Tower_J"};
        for (String node : nodes) {
            activeNodes.add(new Node(node, "5G_MACRO"));
        }

        activeEdges.add(new Edge("Tower_A", "Tower_B", 20));
        activeEdges.add(new Edge("Tower_B", "Tower_D", 20));
        activeEdges.add(new Edge("Tower_A", "Tower_C", 20));
        activeEdges.add(new Edge("Tower_C", "Tower_D", 20));

        activeEdges.add(new Edge("Tower_B", "Tower_E", 20));
        activeEdges.add(new Edge("Tower_C", "Tower_F", 20));
        activeEdges.add(new Edge("Tower_B", "Tower_G", 20));
        activeEdges.add(new Edge("Tower_C", "Tower_H", 20));
        activeEdges.add(new Edge("Tower_D", "Tower_I", 20));
        activeEdges.add(new Edge("Tower_G", "Tower_I", 20));
        activeEdges.add(new Edge("Tower_H", "Tower_I", 20));
        activeEdges.add(new Edge("Tower_I", "Tower_J", 20));

        Gauge.builder("network.edge.latency", latencyAB, AtomicInteger::get).tag("link", "A_to_B").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyBD, AtomicInteger::get).tag("link", "B_to_D").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyAC, AtomicInteger::get).tag("link", "A_to_C").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyCD, AtomicInteger::get).tag("link", "C_to_D").register(meterRegistry);

        Gauge.builder("network.edge.latency", latencyBE, AtomicInteger::get).tag("link", "B_to_E").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyCF, AtomicInteger::get).tag("link", "C_to_F").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyBG, AtomicInteger::get).tag("link", "B_to_G").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyCH, AtomicInteger::get).tag("link", "C_to_H").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyDI, AtomicInteger::get).tag("link", "D_to_I").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyGI, AtomicInteger::get).tag("link", "G_to_I").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyHI, AtomicInteger::get).tag("link", "H_to_I").register(meterRegistry);
        Gauge.builder("network.edge.latency", latencyIJ, AtomicInteger::get).tag("link", "I_to_J").register(meterRegistry);
    }

    public TopologyState getCurrentState() {
        return new TopologyState(activeNodes, activeEdges);
    }

    public void updateEdgeLatency(String source, String destination, int newLatency) {
        for (int i = 0; i < activeEdges.size(); i++) {
            Edge edge = activeEdges.get(i);
            if (edge.sourceId().equals(source) && edge.destinationId().equals(destination)) {
                activeEdges.set(i, new Edge(source, destination, newLatency));

                String path = source + "_" + destination;
                switch (path) {
                    case "Tower_A_Tower_B" -> latencyAB.set(newLatency);
                    case "Tower_B_Tower_D" -> latencyBD.set(newLatency);
                    case "Tower_A_Tower_C" -> latencyAC.set(newLatency);
                    case "Tower_C_Tower_D" -> latencyCD.set(newLatency);
                    case "Tower_B_Tower_E" -> latencyBE.set(newLatency);
                    case "Tower_C_Tower_F" -> latencyCF.set(newLatency);
                    case "Tower_B_Tower_G" -> latencyBG.set(newLatency);
                    case "Tower_C_Tower_H" -> latencyCH.set(newLatency);
                    case "Tower_D_Tower_I" -> latencyDI.set(newLatency);
                    case "Tower_G_Tower_I" -> latencyGI.set(newLatency);
                    case "Tower_H_Tower_I" -> latencyHI.set(newLatency);
                    case "Tower_I_Tower_J" -> latencyIJ.set(newLatency);
                }

                System.out.println("🗺️ [TOPOLOGY UPDATED] " + source + " -> " + destination + " latency now " + newLatency + "ms");
                break;
            }
        }
    }
}