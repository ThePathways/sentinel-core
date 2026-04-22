package com.byteentropy.sentinel_core.api;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TransactionSimulator {

    // Template for Transactions
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    
    // Template for simple Strings (needed for the blacklist)
    private final KafkaTemplate<String, String> stringTemplate;

    @PostMapping("/inject")
    public ResponseEntity<String> injectTransaction(@RequestBody Transaction tx) {
        try {
            kafkaTemplate.send("inbound-transactions", tx.getTransactionId(), tx).get();
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("REAL ERROR: " + e.getMessage());
        }
    }

    @PostMapping("/blacklist/add")
    public ResponseEntity<String> addToBlacklist(@RequestParam String userId) {
        try {
            // Key: userId, Value: reason
            // This populates the GlobalKTable state store
            stringTemplate.send("blacklist-users", userId, "FRAUD_USER").get();
            return ResponseEntity.ok("User " + userId + " added to blacklist topic.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}