package com.aether.simulator.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NetworkCommand(
        String targetNode,
        String action,
        int newEnforcedLatency,
        String humanReadableJustification
) { }
