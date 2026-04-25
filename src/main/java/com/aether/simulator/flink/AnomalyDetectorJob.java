package com.aether.simulator.flink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AnomalyDetectorJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String> builder().setBootstrapServers("localhost:9092")
                .setTopics("network-telemetry").setGroupId("aether-flink-group").setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema()).build();

        DataStream<String> telemetryStream= env.fromSource(source, WatermarkStrategy.noWatermarks(),"Kafka Telemetry Source");

        ObjectMapper mapper = new ObjectMapper();

        DataStream<String> anomalyStream = telemetryStream.filter(jsonString-> {
            try{
                JsonNode jsonNode=  mapper.readTree(jsonString);
                int latency= jsonNode.get("latencyMs").asInt();
                int signal = jsonNode.get("signalStrengthDbm").asInt();

                return latency>100 || signal <-80;
            }
            catch(Exception e)
            {
                return false;
            }
        })
                .map(jsonString->{
                    System.out.println("⚠️ ANOMALY DETECTED! -> " + jsonString);
                    return jsonString.replace("}", ",\"status\":\"CRITICAL_ALERT\"}");
                });

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("network-anomalies")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // Connect the processed stream to the Sink
        anomalyStream.sinkTo(sink);

        System.out.println("Starting Flink Anomaly Detector Job...");
        env.execute("Network Anomaly Detection Job");
    }
}
