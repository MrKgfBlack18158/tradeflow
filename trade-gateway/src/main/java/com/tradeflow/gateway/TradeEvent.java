package com.tradeflow.gateway;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The message published to Kafka. It carries a tradeId generated here, at the edge,
 * so every downstream service can use it to detect duplicates (idempotency).
 */
public record TradeEvent(
        String tradeId,
        String account,
        String symbol,
        Side side,
        long quantity,
        BigDecimal price,
        Instant receivedAt
) {

    public static TradeEvent from(TradeRequest request) {
        return new TradeEvent(
                UUID.randomUUID().toString(),
                request.account(),
                request.symbol().toUpperCase(),
                request.side(),
                request.quantity(),
                request.price(),
                Instant.now()
        );
    }
}
