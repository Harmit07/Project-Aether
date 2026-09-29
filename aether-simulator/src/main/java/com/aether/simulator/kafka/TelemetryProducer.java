package com.aether.simulator.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class TelemetryProducer {

    private final KafkaProducer<String,String> producer;
    private final String topic = "network-telemetry";

    public TelemetryProducer()
    {
        Properties properties= new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer= new KafkaProducer<>(properties);

    }

    public void sendLog(String nodeId, String jsonLog)
    {
        ProducerRecord<String, String> record= new ProducerRecord<>(topic,nodeId,jsonLog);
        producer.send(record);
    }
    public void close()
    {
        producer.close();
    }
}
