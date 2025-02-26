# Quantum Threat Risk Assessment (QTRA)

## Overview
The Quantum Threat Risk Assessment (QTRA) project evaluates a domain’s TLS security posture and its readiness for post-quantum cryptography (PQC). It automates TLS scanning, risk analysis, and reporting, using a chained AI agent architecture to classify quantum safety and generate a Quantum Readiness Score.

## Features
- Automated TLS Scanning – Checks TLS version, cipher suites, and security features like HSTS & DNSSEC.
- AI-Driven Quantum Risk Analysis – Classifies encryption strength and assigns a readiness score.
- Kafka-Driven Event Processing – Uses Spring Boot + Kafka for scalability and real-time processing.
- AI-Powered Reporting – Produces a structured security report for monitoring and integration.
- Chained AI Agent Architecture – Ensures modular and extensible analysis flow.

## Tech Stack
- Spring Boot (Backend Framework)
- Kafka (Event-Driven Processing)
- Docker & Kafka UI (Monitoring)
- Swagger UI (API Testing)
- Jackson (JSON Processing)
- Lombok (Code Simplification)

## AI Agents & Processing Flow
QTRA is designed as a chained AI agent system, where each agent specializes in a specific task:

### 1. TLS Scanner Agent (SSLScannerAgent)
- Scans the provided domain and subdomains for:
   - TLS version
   - Cipher suite
   - HSTS & DNSSEC security features
- Publishes raw results to Kafka (`tls-scan-results` topic).

### 2. Quantum Risk Analyzer Agent (QuantumRiskAnalyzerAgent)
- Consumes TLS scan results from Kafka.
- Classifies the quantum safety level of the encryption.
- Computes a Quantum Readiness Score, considering:
   - Cipher strength
   - TLS version
   - Post-quantum cryptographic support
   - HSTS and DNSSEC security checks
- Publishes enriched results to Kafka (`tls-analysis-results` topic).

### 3. Report Generator Agent (ReportGeneratorAgent)
- Consumes quantum readiness results from Kafka.
- Generates a structured Quantum Readiness Report.
- Designed for future integration with dashboards or databases.

## Running the Project

### 1. Clone the Repository
```sh
git clone https://github.com/himaschal/qtra.git
cd qtra
```

### 2. Build the package and run the Application with Docker Compose
```sh
mvn clean package
docker compose -d --build
```

This will start:

- The QTRA application
- Kafka and Zookeeper for event-driven processing
- Kafka UI for monitoring

### 3. Access Kafka UI
Kafka UI will be available at:
http://localhost:8080/

## Submitting a TLS Scan

### 1. Access Swagger UI
   http://localhost:8081/swagger-ui.html

### 2. Submit a Scan Request
   - Open Swagger UI
   - Go to `GET /tls/scan`
   - Enter a domain (e.g., `aws.com`)
   - Click `Execute`

## Monitoring Messages in Kafka UI

### 1. View TLS Scan Results (tls-scan-results topic)
   - Open Kafka UI at http://localhost:8080/
   - Navigate to "Topics" → `tls-scan-results`
   - Click "Messages" to view the raw scan results.

### 2. View Quantum Readiness Analysis (tls-analysis-results topic)
   - In Kafka UI, go to "Topics" → `tls-analysis-results`
   - Click "Messages" to see the Quantum Readiness Score and analysis details.

## Stopping the Application
To stop all running services, use:

```sh
docker-compose down
```

## Future Enhancements ##
- Improve AI Agents – Enhance risk scoring using real-time threat intelligence.
- Database Integration – Store historical TLS scans for tracking trends.
- Security Dashboard – Visualize readiness scores in Grafana.
- Extend API Exposure – Allow external integrations for real-time security assessments.

## Troubleshooting ##
- Ensure that the container ports are available
