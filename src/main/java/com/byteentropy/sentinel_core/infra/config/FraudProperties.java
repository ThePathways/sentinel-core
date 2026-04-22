package com.byteentropy.sentinel_core.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "fraud-engine")
public class FraudProperties {
    
    private Thresholds thresholds = new Thresholds();
    // FIX: Change Map<String, Integer> to the Weights class
    private Weights weights = new Weights(); 

    @Data
    public static class Thresholds {
        private BigDecimal highAmount;
        private int reviewScore;
        private int rejectScore;
    }

    @Data
    public static class Weights {
        private int highAmountRule;
        private int blacklistRule;
        private int velocityRule;     // Added for future-proofing
        private int deviceRiskRule;   // Added for future-proofing
    }
}
