package com.tradeflow.processor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Stored in the "trades" collection.
 * _id = tradeId, so saving the same trade twice overwrites instead of duplicating:
 * this is what makes the consumer idempotent when Kafka redelivers a message.
 * DECIMAL128 keeps money exact in MongoDB (the default would store BigDecimal as a string).
 */
@Document("trades")
public record TradeDocument(
        @Id String tradeId,
        @Indexed String account,
        String symbol,
        String side,
        long quantity,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal price,
        @Field(targetType = FieldType.DECIMAL128) BigDecimal notional,
        TradeStatus status,
        String rejectReason,
        Instant receivedAt,
        Instant processedAt
) {
}
