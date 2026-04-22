package com.byteentropy.sentinel_core.domain.rules;

import com.byteentropy.sentinel_core.domain.model.Transaction;

public interface FraudRule {
    /** @return Unique name of the rule */
    String getName();

    /** @return Risk weight assigned to this rule */
    int getWeight();

    /** @return true if the transaction is suspicious */
    boolean evaluate(Transaction transaction);
}