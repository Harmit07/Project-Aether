package com.aether.simulator.engine;

import com.aether.simulator.kafka.CommandListener;
import com.aether.simulator.kafka.TelemetryProducer;
import com.aether.simulator.model.Edge;
import com.aether.simulator.model.NetworkGraph;
import com.aether.simulator.model.Node;
import com.aether.simulator.model.TelemetryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulatorEngine {
    public static void main(String[] args) throws Exception {
        NetworkGraph network = new NetworkGraph();

        Node towerA = new Node("Tower_A", "5G_MACRO");
        Node towerB = new Node("Tower_B", "5G_MACRO");
        Node towerC = new Node("Tower_C", "5G_MACRO");
        Node towerD = new Node("Tower_D", "5G_MACRO");
        Node towerE = new Node("Tower_E", "5G_MACRO");
        Node towerF = new Node("Tower_F", "5G_MACRO");
        Node towerG = new Node("Tower_G", "5G_MACRO");
        Node towerH = new Node("Tower_H", "5G_MACRO");
        Node towerI = new Node("Tower_I", "5G_MACRO");
        Node towerJ = new Node("Tower_J", "5G_MACRO");

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

            for (Node node : network.getAdjacencyList().keySet()) {
                for (Edge edge : network.getAdjacencyList().get(node)) {

                    int currentLatency;
                    int currentSignal = -60;

                    if (random.nextInt(100) < 10) {
                        currentLatency = 150 + random.nextInt(300);
                        System.out.println(" NATURAL ANOMALY DETECTED: " +
                                edge.getSource().getNodeId() + " -> " + edge.getDestination().getNodeId() +
                                " [" + currentLatency + "ms]");
                    } else {
                        currentLatency = 15 + random.nextInt(20);
                    }

                    edge.getTelemetry().updateMetrics(currentLatency, currentSignal);

                    TelemetryEvent event = new TelemetryEvent(
                            System.currentTimeMillis(),
                            edge.getSource().getNodeId(),
                            edge.getDestination().getNodeId(),
                            currentLatency,
                            currentSignal
                    );

                    String jsonLog = jsonMapper.writeValueAsString(event);
                    kafkaProducer.sendLog(edge.getSource().getNodeId(), jsonLog);
                }
            }

            System.out.println("Network telemetry batch sent to Kafka.");
            Thread.sleep(20000);
        }

    }
}
