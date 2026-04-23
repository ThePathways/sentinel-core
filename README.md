![Java CI with Maven](https://github.com/ThePathways/sentinel-core/actions/workflows/maven.yml/badge.svg)
![Status](https://img.shields.io/badge/Aiven-Ready-purple?style=flat-square)

# Sentinel Core 🛡️

Sentinel Core is a high-performance, event-driven fraud detection engine built with **Spring Boot**, **Kafka Streams**, and **H2**. It processes financial transactions in real-time using a weighted scoring model to identify and block suspicious activity.

## 🚀 Key Features
- **Modern Java Stack**: Optimized for **Java 25**, leveraging the latest JVM performance enhancements.
- **Stream Processing**: Sub-second fraud evaluation via Kafka Streams.
- **Global State Store**: Real-time user blacklisting using `GlobalKTable` synchronized across all nodes.
- **Asynchronous Audit**: Decisions are saved to H2 and broadcast to a `fraud-decisions` topic without blocking the transaction flow.

## 🛠️ Architecture
1. **Producer**: Transactions enter via REST -> Kafka `inbound-transactions`.
2. **Processor**: `ScoringEngine` runs rules (Blacklist, High Amount, etc.).
3. **Sink**: Results are persisted to H2 and sent to a decision topic.
## ⚙️ Configuration & Security
This project is configured for **SSL-secured Kafka** (e.g., Aiven, Confluent). For security, sensitive credentials must be externalized using environment variables.

### Environment Variables Required:
| Variable | Description |
| :--- | :--- |
| `KAFKA_BOOTSTRAP_SERVERS` | Your Kafka Broker URL |
| `SSL_TRUSTSTORE_LOCATION` | Absolute path to your `.jks` or `.p12` truststore |
| `SSL_KEYSTORE_LOCATION` | Absolute path to your keystore |
| `SSL_PASSWORD` | Password for your SSL certificates |

## 🧪 Quick Start & Testing

### 1. Start the Application
Ensure your Kafka cluster is running and your environment variables are set, then run:
```bash
mvn spring-boot:run
```

### 2. Inject a Clean Transaction

```bash
curl -X POST http://localhost:8081/test/inject \
-H "Content-Type: application/json" \
-d '{
  "transactionId": "tx-101",
  "userId": "user_demo",
  "amount": 250.00,
  "currency": "USD",
  "ipAddress": "1.1.1.1",
  "timestamp": 1713772800
}'
```

Expected Result: APPROVE (Score: 0)

### 3. Blacklist a User Globally

```bash
curl -X POST "http://localhost:8081/test/blacklist/add?userId=MALICIOUS_USER"
```

4. Trigger the Fraud Rule

```bash
curl -X POST http://localhost:8081/test/inject \
-H "Content-Type: application/json" \
-d '{
  "transactionId": "tx-102",
  "userId": "MALICIOUS_USER",
  "amount": 10.00,
  "ipAddress": "1.1.1.1",
  "timestamp": 1713772800
}'
```

Expected Result: REJECT (Score: 100, Reason: Global User Blacklist)

## 📊 Audit Trail

You can inspect the decision history via the built-in H2 Console:

```
URL: http://localhost:8081/h2-console/

JDBC URL: jdbc:h2:mem:fraud_db

Query: SELECT * FROM RISK_RESULT ORDER BY processed_at DESC;
```

## 🛡️ License

This project is licensed under the MIT License.

