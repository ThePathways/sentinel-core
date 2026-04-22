package com.byteentropy.sentinel_core.infra.serde;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import com.byteentropy.sentinel_core.domain.model.RiskResult;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

/**
 * FINAL 2026 VERSION: Zero Deprecations
 * Uses the JacksonJsonSerde family (Spring Kafka 4.0+).
 */
public class AppSerdes {

    public static Serde<Transaction> Transaction() {
        // JacksonJsonSerde is the official replacement for JsonSerde in 4.0
        return new JacksonJsonSerde<>(Transaction.class);
    }

    public static Serde<RiskResult> RiskResult() {
        return new JacksonJsonSerde<>(RiskResult.class);
    }
}