package com.byteentropy.sentinel_core.api;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import com.byteentropy.sentinel_core.domain.service.ScoringEngine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
public class ManualRunner {

    @Bean
    @Profile("!test") // This won't run during your 'mvn install' tests
    public CommandLineRunner runDemo(ScoringEngine engine) {
        return args -> {
            System.out.println("\n--- STARTING MANUAL DEMO (NO KAFKA REQUIRED) ---");
            
            Transaction tx = Transaction.builder()
                    .transactionId("MANUAL-101")
                    .userId("USER_SCAMMER_99")
                    .amount(new BigDecimal("15000.00"))
                    .build();

            engine.scoreAndLog(tx);
            
            System.out.println("--- DEMO COMPLETE check the logs above --- \n");
        };
    }
}