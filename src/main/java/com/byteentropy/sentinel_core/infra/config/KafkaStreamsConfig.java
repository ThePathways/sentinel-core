package com.byteentropy.sentinel_core.infra.config;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import com.byteentropy.sentinel_core.domain.service.ScoringEngine;
import com.byteentropy.sentinel_core.infra.serde.AppSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    public static final String BLACKLIST_STORE = "blacklist-state-store";

    @Bean
    public KStream<String, Transaction> processFraud(StreamsBuilder builder, ScoringEngine engine) {
        
        // 1. Create the GlobalTable for the blacklist 
        // This is synchronized across all instances of your microservice
        builder.globalTable("blacklist-users", 
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(BLACKLIST_STORE));

        // 2. Consume the raw inbound transactions
        KStream<String, Transaction> input = builder.stream("inbound-transactions", 
                Consumed.with(Serdes.String(), AppSerdes.Transaction()));

        // 3. The Processing Pipeline
        input
             // Logging for visibility (Remove in high-volume prod logs if needed)
             .peek((k, v) -> System.out.println(">>> STEP 1: RECEIVED FROM KAFKA: " + k))
             
             // The Core Logic: Scores the TX and saves to H2 Audit Log
             .mapValues(tx -> {
                 System.out.println(">>> STEP 2: SCORING TX: " + tx.getTransactionId());
                 return engine.scoreAndLog(tx);
             })
             
             // Final confirmation before broadcasting
             .peek((k, result) -> System.out.println(">>> STEP 3: LOGGED TO H2 & BROADCASTING RESULT: " + result.getAction()))
             
             // 4. Output the decision to the "Source of Truth" topic
             .to("fraud-decisions", Produced.with(Serdes.String(), AppSerdes.RiskResult()));

        return input;
    }
}