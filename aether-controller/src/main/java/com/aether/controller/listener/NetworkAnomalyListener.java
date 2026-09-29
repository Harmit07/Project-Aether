package com.aether.controller.listener;


import com.aether.controller.model.NetworkModels;
import com.aether.controller.model.NetworkModels.AnomalyAlert;
import com.aether.controller.service.AiRemediationService;
import com.aether.controller.service.NetworkTopologyService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service

public class NetworkAnomalyListener {

    private final ObjectMapper objectMapper;
    private final NetworkTopologyService topologyService;
    private final AiRemediationService aiRemediationService;

    public NetworkAnomalyListener(NetworkTopologyService topologyService,AiRemediationService aiRemediationService) {
        this.objectMapper = new ObjectMapper();
        this.topologyService = topologyService;
        this.aiRemediationService = aiRemediationService;
    }
    @KafkaListener(topics="network-anomalies", groupId="aether-sdn-controller")
    public void handleCriticalAlert(String anomalyJson) {
        try {
            System.out.println("\n🚨 [SDN CONTROLLER] CRITICAL NETWORK ALERT RECEIVED 🚨");

            AnomalyAlert alert = objectMapper.readValue(anomalyJson, AnomalyAlert.class);

            topologyService.updateEdgeLatency(alert.sourceNode(), alert.destinationNode(), alert.latencyMs());

            String contextForAi = "ALERT: " + alert.sourceNode() + " to " + alert.destinationNode() + " is experiencing " + alert.latencyMs() + "ms latency. \n" +
                    "CURRENT TOPOLOGY: " + topologyService.getCurrentState().toString();


            aiRemediationService.analyzeAndFixNetwork(contextForAi);

        } catch (Exception e) {
            System.err.println("Failed to process anomaly alert: " + e.getMessage());
        }
    }

}
