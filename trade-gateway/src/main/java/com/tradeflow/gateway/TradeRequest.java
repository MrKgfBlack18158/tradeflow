package com.tradeflow.gateway;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * What a client POSTs to /api/trades.
 * Bean Validation rejects bad input with HTTP 400 before anything reaches Kafka.
 * BigDecimal (not double) for money: doubles can't represent values like 0.1 exactly.
 */
public record TradeRequest(
        @NotBlank String account,
        @NotBlank String symbol,
        @NotNull Side side,
        @Positive long quantity,
        @NotNull @Positive BigDecimal price
) {
}
