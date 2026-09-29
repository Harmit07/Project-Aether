# Project Aether: Autonomous AI Network Controller

Project Aether is a proof-of-concept for an autonomous Software-Defined Networking (SDN) system. It simulates a live network topology, streams telemetry data, detects anomalies in real-time using stream processing, and leverages an AI Agent (via Groq and Llama 3) to automatically generate network remediation policies. The entire flow is visualized on a real-time React dashboard.

## 🏗 Architecture & Data Flow

The system is composed of several micro-components working together asynchronously:

1. **Simulator ➡️ Kafka:** The Java simulator generates real-time telemetry data for simulated 5G Macro towers and streams it to Apache Kafka.
2. **Kafka ➡️ Flink:** Apache Flink continuously processes the telemetry streams to detect latency anomalies.
3. **Flink ➡️ Kafka:** When an anomaly is detected, Flink publishes an alert back to a Kafka topic.
4. **Kafka ➡️ Spring Boot:** A Spring Boot listener consumes the anomaly alerts.
5. **Spring Boot ➡️ Groq AI:** The Spring backend sends the network context to the Groq API (running a Llama 3 model), which acts as an autonomous agent to determine the best remediation policy (e.g., routing changes, latency adjustments).
6. **Spring Boot ➡️ WebSockets:** The AI's remediation policy is pushed to the frontend via WebSockets.
7. **WebSockets ➡️ React Dashboard:** The Vite/React dashboard visualizes the network topology and live remediation actions in real-time.

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 23 (or compatible)
- Maven
- Node.js & npm
- A Groq API key (configured in the Spring Boot application properties)

### Running the Stack locally

You will need multiple terminal windows to run all components concurrently.

#### 1. Start Infrastructure (Kafka, Zookeeper, Prometheus, Grafana)

```bash
docker-compose up -d
```

#### 2. Start the Backend Controller

```bash
cd aether-controller
mvn spring-boot:run
```

#### 3. Start the Flink Anomaly Detector

```bash
cd aether-simulator
mvn clean compile exec:java -Dexec.mainClass="com.aether.simulator.flink.AnomalyDetectorJob"
```

#### 4. Start the Simulator Engine

```bash
cd aether-simulator
mvn clean compile exec:java -Dexec.mainClass="com.aether.simulator.engine.SimulatorEngine"
```

#### 5. Start the React Dashboard

```bash
cd aether-dashboard
npm install
npm run dev
```

The dashboard will be available at `http://localhost:5173`.

## 📁 Project Structure

- `aether-controller/`: Spring Boot backend that handles AI communication and WebSockets.
- `aether-simulator/`: Java application containing the telemetry engine and Flink processing jobs.
- `aether-dashboard/`: React + Vite frontend for network visualization.
- `docker-compose.yml`: Infrastructure configuration.
