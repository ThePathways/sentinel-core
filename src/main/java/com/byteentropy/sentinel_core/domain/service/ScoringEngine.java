package com.byteentropy.sentinel_core.domain.service;

import com.byteentropy.sentinel_core.domain.model.*;
import com.byteentropy.sentinel_core.domain.rules.FraudRule;
import com.byteentropy.sentinel_core.infra.config.FraudProperties;
import com.byteentropy.sentinel_core.infra.repository.RiskResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringEngine {

    private final List<FraudRule> rules;
    private final FraudProperties props;
    private final RiskResultRepository repository;

    /**
     * Executes all registered fraud rules and persists the outcome.
     * @param tx The inbound transaction to evaluate.
     * @return The saved RiskResult containing the score and decision.
     */
    public RiskResult scoreAndLog(Transaction tx) {
        if (tx == null || tx.getTransactionId() == null) {
            log.error("[ENGINE] Received null or invalid transaction. Aborting.");
            return null;
        }

        log.debug("[ENGINE] Evaluating Transaction: {}", tx.getTransactionId());

        // 1. EVALUATE ALL RULES
        // Collects rules that return 'true' for evaluate()
        List<FraudRule> triggeredRules = rules.stream()
                .filter(rule -> {
                    try {
                        return rule.evaluate(tx);
                    } catch (Exception e) {
                        log.error("[RULE ERROR] Failed to evaluate rule '{}' for TX: {}", 
                                  rule.getName(), tx.getTransactionId(), e);
                        return false; 
                    }
                })
                .toList();

        // 2. AGGREGATE RESULTS
        int totalScore = triggeredRules.stream()
                .mapToInt(FraudRule::getWeight)
                .sum();

        String action = determineAction(totalScore);

        // 3. FORMAT AUDIT REASON
        // Join the names of triggered rules (e.g., "HighAmountRule, BlacklistRule")
        String internalReason = triggeredRules.isEmpty() 
                ? "APPROVED: No suspicious patterns detected" 
                : triggeredRules.stream()
                                .map(FraudRule::getName)
                                .collect(Collectors.joining(", "));

        log.info("[DECISION] TX: {} | Score: {} | Action: {} | Reasons: [{}]", 
                 tx.getTransactionId(), totalScore, action, internalReason);

        // 4. PERSISTENCE
        RiskResult result = RiskResult.builder()
                .transactionId(tx.getTransactionId())
                .userId(tx.getUserId()) // Don't forget to map this!
                .score(totalScore)
                .action(action)
                .reason(internalReason)
                .build();

        return repository.save(result);
    }

    /**
     * Logic for categorizing the risk score based on configured thresholds.
     */
    private String determineAction(int score) {
        if (score >= props.getThresholds().getRejectScore()) {
            return "REJECT";
        } else if (score >= props.getThresholds().getReviewScore()) {
            return "REVIEW";
        }
        return "APPROVE";
    }
}