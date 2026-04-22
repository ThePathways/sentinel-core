package com.byteentropy.sentinel_core.domain.rules;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import com.byteentropy.sentinel_core.infra.config.FraudProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HighAmountRule implements FraudRule {

    private final FraudProperties props;

    @Override
    public String getName() {
        return "HIGH_AMOUNT_LIMIT";
    }

    @Override
    public int getWeight() {
        // FIX: Now calls the method directly
        return props.getWeights().getHighAmountRule();
    }

    @Override
    public boolean evaluate(Transaction tx) {
        return tx.getAmount().compareTo(props.getThresholds().getHighAmount()) > 0;
    }
}
