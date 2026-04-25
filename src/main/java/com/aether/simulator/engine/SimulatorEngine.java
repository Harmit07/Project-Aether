package com.aether.simulator.engine;

import com.aether.simulator.kafka.CommandListener;
import com.aether.simulator.kafka.TelemetryProducer;
import com.aether.simulator.model.Edge;
import com.aether.simulator.model.NetworkGraph;
import com.aether.simulator.model.Node;
import com.aether.simulator.model.TelemetryEvent;
// ✅ Correct Jackson Import
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulatorEngine {
    public static void main(String[] args) throws Exception {
        NetworkGraph network = new NetworkGraph();

        // Define Nodes
        Node towerA = new Node("TowerA", "5G_MACRO");
        Node towerB = new Node("TowerB", "5G_MACRO");
        Node towerC = new Node("TowerC", "5G_MACRO");
        Node towerD = new Node("TowerD", "5G_MACRO");
        Node towerE = new Node("Tower_E", "5G_MACRO");
        Node towerF = new Node("Tower_F", "5G_MACRO");
        Node towerG = new Node("Tower_G", "5G_MACRO");
        Node towerH = new Node("Tower_H", "5G_MACRO");
        Node towerI = new Node("Tower_I", "5G_MACRO");
        Node towerJ = new Node("Tower_J", "5G_MACRO");

        // Define Edges
        network.addEdge(towerA, towerB);
        network.addEdge(towerB, towerD);
        network.addEdge(towerA, towerC);
        network.addEdge(towerC, towerD);
        network.addEdge(towerB, towerE);
        network.addEdge(towerC, towerF);
        network.addEdge(towerB, towerG);
        network.addEdge(towerC, towerH);
        network.addEdge(towerD, towerI);
        network.addEdge(towerG, towerI);
        network.addEdge(towerH, towerI);
        network.addEdge(towerI, towerJ);

        Random random = new Random();
        ObjectMapper jsonMapper = new ObjectMapper();
        TelemetryProducer kafkaProducer = new TelemetryProducer();

        System.out.println("Starting simulation...");

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaProducer::close));

        CommandListener commandListener = new CommandListener(network);
        new Thread(commandListener).start();

        while (true) {
            System.out.println("\n--- Generating New Network State ---");

            // No more victimEdge selection!
            // We loop through EVERY node and EVERY edge in the graph.
            for (Node node : network.getAdjacencyList().keySet()) {
                for (Edge edge : network.getAdjacencyList().get(node)) {

                    int currentLatency;
                    int currentSignal = -60;

                    // --- NATURAL ANOMALY LOGIC ---
                    // We generate a number between 0 and 99.
                    // If it's less than 10 (10% chance), a natural spike occurs.
                    if (random.nextInt(100) < 10) {
                        // NATURAL SPIKE: 150ms to 450ms
                        currentLatency = 150 + random.nextInt(300);
                        System.out.println(" NATURAL ANOMALY DETECTED: " +
                                edge.getSource().getNodeId() + " -> " + edge.getDestination().getNodeId() +
                                " [" + currentLatency + "ms]");
                    } else {
                        // NORMAL STATE: 15ms to 35ms
                        currentLatency = 15 + random.nextInt(20);
                    }

                    // Update the internal edge memory
                    edge.getTelemetry().updateMetrics(currentLatency, currentSignal);

                    // Create the event snapshot
                    TelemetryEvent event = new TelemetryEvent(
                            System.currentTimeMillis(),
                            edge.getSource().getNodeId(),
                            edge.getDestination().getNodeId(),
                            currentLatency,
                            currentSignal
                    );

                    // Send to Kafka
                    String jsonLog = jsonMapper.writeValueAsString(event);
                    kafkaProducer.sendLog(edge.getSource().getNodeId(), jsonLog);
                }
            }

            System.out.println("Network telemetry batch sent to Kafka.");
            Thread.sleep(20000);
        }

    }
}
