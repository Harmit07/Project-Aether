package com.aether.simulator.kafka;
import com.aether.simulator.model.Edge;
import com.aether.simulator.model.NetworkCommand;
import com.aether.simulator.model.NetworkGraph;
import com.aether.simulator.model.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.aether.simulator.routing.DijkstraRouter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class CommandListener implements Runnable{

    private final KafkaConsumer<String,String> consumer;
    private final NetworkGraph networkGraph;
    private final ObjectMapper objectMapper;

    public CommandListener(NetworkGraph networkGraph) {
        this.networkGraph = networkGraph;
        this.objectMapper = new ObjectMapper();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "simulator-command-listener");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList("network-commands"));
    }

    @Override
    public void run()
    {
        System.out.println(" Command Listener Thread started. Waiting for AI instructions...");

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {
                    NetworkCommand command = objectMapper.readValue(record.value(), NetworkCommand.class);

                    System.out.println("\n⚡ [SIMULATOR] AI COMMAND RECEIVED: " + command.action() + " " + command.targetNode());
                    System.out.println("Reason: " + command.humanReadableJustification());

                    applyCommandToNetwork(command);
                }
            }
        } catch (Exception e) {
            System.err.println("Listener error: " + e.getMessage());
        } finally {
            consumer.close();
        }
    }
    private void applyCommandToNetwork(NetworkCommand command) {
        for (Node node : networkGraph.getAdjacencyList().keySet()) {
            List<Edge> edges = networkGraph.getAdjacencyList().get(node);
            for (Edge edge : edges) {
                if (edge.getSource().getNodeId().equalsIgnoreCase(command.targetNode()) ||
                        edge.getDestination().getNodeId().equalsIgnoreCase(command.targetNode())) {

                    int signal = "QUARANTINE".equalsIgnoreCase(command.action()) ? -999 : -60;
                    edge.getTelemetry().updateMetrics(command.newEnforcedLatency(), signal);
                    System.out.println("🔧 ENFORCED: Link " + edge.getSource().getNodeId() + " -> " +
                            edge.getDestination().getNodeId() + " updated to " + command.newEnforcedLatency() + "ms");
                }
            }
        }

        recalculateShortestPath();
    }

    private void recalculateShortestPath() {
        Node sourceNode = networkGraph.findNodeById("Tower_A");
        Node destNode = networkGraph.findNodeById("Tower_J");

        if (sourceNode != null && destNode != null) {
            List<Node> fastestPath = DijkstraRouter.findFastestPath(networkGraph, sourceNode, destNode);
            int totalLatency = 0;
            StringBuilder pathStr = new StringBuilder();

            for (int i = 0; i < fastestPath.size(); i++) {
                pathStr.append(fastestPath.get(i).getNodeId());
                if (i < fastestPath.size() - 1) {
                    pathStr.append(" -> ");
                    Node curr = fastestPath.get(i);
                    Node next = fastestPath.get(i + 1);
                    for (Edge edge : networkGraph.getAdjacencyList().getOrDefault(curr, Collections.emptyList())) {
                        if (edge.getDestination().equals(next)) {
                            totalLatency += edge.getTelemetry().getLatencyMs();
                            break;
                        }
                    }
                }
            }
            System.out.println("🗺️ [DIJKSTRA RE-ROUTING] Updated Optimal Route: " + pathStr + " (Total Latency: " + totalLatency + "ms)\n");
        }
    }
}
