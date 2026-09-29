package com.aether.controller.service;

import com.aether.controller.model.NetworkModels.RemediationPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiRemediationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SimpMessagingTemplate webSocketTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    public AiRemediationService(KafkaTemplate<String, String> kafkaTemplate, SimpMessagingTemplate webSocketTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.webSocketTemplate = webSocketTemplate;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void analyzeAndFixNetwork(String networkContext) {
        try {
            System.out.println("🤖 Bypassing framework bugs... Sending state directly to Groq API.");

            String url = "https://api.groq.com/openai/v1/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "llama-3.1-8b-instant");
            requestBody.putObject("response_format").put("type", "json_object");

            ArrayNode messages = requestBody.putArray("messages");

            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an autonomous Software-Defined Networking (SDN) AI agent. A critical network anomaly has been detected. Review the current network topology and the alert context provided. Your job is to generate a Remediation Policy to fix the network routing. You must output a strict JSON object with four exact keys: 'targetNode' (string), 'action' (string), 'newEnforcedLatency' (number), and 'humanReadableJustification' (string).");

            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", networkContext);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String aiContent = rootNode.path("choices").get(0).path("message").path("content").asText();
            RemediationPolicy policy = objectMapper.readValue(aiContent, RemediationPolicy.class);

            System.out.println("\n✅ [AI REMEDIATION POLICY GENERATED]");
            System.out.println("Target Node: " + policy.targetNode());
            System.out.println("Action: " + policy.action());
            System.out.println("New Enforced Latency: " + policy.newEnforcedLatency() + "ms");
            System.out.println("AI Justification: " + policy.humanReadableJustification());

            String commandJson = objectMapper.writeValueAsString(policy);
            kafkaTemplate.send("network-commands", commandJson);
            webSocketTemplate.convertAndSend("/topic/ai-commands", commandJson);

            System.out.println(">> COMMAND DEPLOYED TO MESSAGE BROKER AND WEBSOCKET!");

        } catch (Exception e) {
            System.err.println("❌ Groq API Failed: " + e.getMessage());
        }
    }
}