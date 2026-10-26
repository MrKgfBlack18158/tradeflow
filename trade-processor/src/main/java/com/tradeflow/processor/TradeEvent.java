package com.tradeflow.processor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The processor's own copy of the message contract. Each service owns its classes;
 * they only agree on the JSON shape. (Later: a shared schema, e.g. Avro + Schema Registry.)
 */
public record TradeEvent(
        String tradeId,
        String account,
        String symbol,
        String side,
        long quantity,
        BigDecimal price,
        Instant receivedAt
) {
}
