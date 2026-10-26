package com.tradeflow.query;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read model over the "trades" collection written by trade-processor.
 * Separate write side (processor) and read side (query) is a light form of CQRS.
 */
@Document("trades")
public record TradeView(
        @Id String tradeId,
        String account,
        String symbol,
        String side,
        long quantity,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal price,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal notional,
        String status,
        String rejectReason,
        Instant receivedAt,
        Instant processedAt
) {
}
