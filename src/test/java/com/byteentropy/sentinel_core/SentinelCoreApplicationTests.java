package com.byteentropy.sentinel_core;

import com.byteentropy.sentinel_core.domain.model.*;
import com.byteentropy.sentinel_core.domain.rules.BlacklistRule;
import com.byteentropy.sentinel_core.domain.service.ScoringEngine;
import com.byteentropy.sentinel_core.infra.config.KafkaStreamsConfig;
import com.byteentropy.sentinel_core.infra.repository.RiskResultRepository;
import com.byteentropy.sentinel_core.infra.serde.AppSerdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SentinelGuardIntegrationTest {

    @Autowired private ScoringEngine scoringEngine;
    @Autowired private BlacklistRule blacklistRule; 
    @Autowired private RiskResultRepository repository;

    private TopologyTestDriver td;
    private TestInputTopic<String, Transaction> inputTopic;
    private TestInputTopic<String, String> blacklistTopic;
    private TestOutputTopic<String, RiskResult> outputTopic;

    @BeforeEach
    void setup() {
        StreamsBuilder builder = new StreamsBuilder();
        new KafkaStreamsConfig().processFraud(builder, scoringEngine);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-logic-" + System.currentTimeMillis());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "mock:1234");

        td = new TopologyTestDriver(builder.build(), props);
        
        // --- BRIDGE FIX: Link TestDriver memory to the BlacklistRule Bean ---
        blacklistRule.setTestStore(td.getKeyValueStore(KafkaStreamsConfig.BLACKLIST_STORE));

        inputTopic = td.createInputTopic("inbound-transactions", 
                new StringSerializer(), AppSerdes.Transaction().serializer());
        blacklistTopic = td.createInputTopic("blacklist-users", 
                new StringSerializer(), new StringSerializer());
        outputTopic = td.createOutputTopic("fraud-decisions", 
                new StringDeserializer(), AppSerdes.RiskResult().deserializer());
        
        repository.deleteAll(); 
    }

    @Test
    @DisplayName("Verify Rule Summing: Blacklist(100) + HighAmount(55) = 155")
    void testSummingLogic() {
        // 1. Add to Blacklist
        blacklistTopic.pipeInput("SCAMMER_01", "Fraud");
        
        // 2. High Amount Transaction from Scammer
        Transaction tx = Transaction.builder()
                .transactionId("TX-SUM")
                .userId("SCAMMER_01")
                .amount(new BigDecimal("15000.00")) // > 10,000 threshold
                .build();

        inputTopic.pipeInput(tx.getTransactionId(), tx);
        RiskResult result = outputTopic.readValue();
        
        assertEquals(155, result.getScore(), "Both rules should have triggered");
        assertEquals("REJECT", result.getAction());
    }

    @AfterEach
    void tearDown() {
        if (td != null) {
            blacklistRule.setTestStore(null); // Clean up for next test
            td.close();
        }
    }
}