package com.byteentropy.sentinel_core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String ipAddress;
    private Long timestamp;
}
