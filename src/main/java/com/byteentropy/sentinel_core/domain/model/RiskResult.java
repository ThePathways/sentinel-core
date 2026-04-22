package com.byteentropy.sentinel_core.domain.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Column;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RiskResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "transaction_id")
    private String transactionId; 

    @Column(name = "user_id")
    private String userId; // CRITICAL: Links the decision to the person
    
    private int score;
    private String action; 
    private String reason;
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }
}