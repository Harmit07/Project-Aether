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
                // Poll Kafka every 500ms for new commands
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
        // If the AI says to QUARANTINE a node, we find all edges connected to it
        // and permanently set their latency to the AI's enforced weight (e.g., 9999)
        if ("QUARANTINE".equalsIgnoreCase(command.action())) {
            for (Node node : networkGraph.getAdjacencyList().keySet()) {
                List<Edge> edges = networkGraph.getAdjacencyList().get(node);
                for (Edge edge : edges) {
                    if (edge.getSource().getNodeId().equals(command.targetNode()) ||
                            edge.getDestination().getNodeId().equals(command.targetNode())) {

                        // We use a massive negative signal strength to represent a "dead" link
                        edge.getTelemetry().updateMetrics(command.newEnforcedLatency(), -999);
                        System.out.println("🔧 ENFORCED: Link " + edge.getSource().getNodeId() + " -> " +
                                edge.getDestination().getNodeId() + " locked to " + command.newEnforcedLatency() + "ms");
                    }
                }
            }
        }
    }
}
