package com.tradeflow.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
public class TradeListener {

    private static final Logger log = LoggerFactory.getLogger(TradeListener.class);

    private final TradeRepository repository;
    private final TradeValidator validator;
    private final Clock clock;

    public TradeListener(TradeRepository repository, TradeValidator validator, Optional<Clock> clock) {
        this.repository = repository;
        this.validator = validator;
        this.clock = clock.orElse(Clock.systemUTC());
    }

    /**
     * Called once per Kafka message. If this method throws, Spring Kafka's default error handler
     * retries the message (9 retries, no backoff) and then logs and skips it.
     * TODO (Fri session): add a DefaultErrorHandler with backoff + DeadLetterPublishingRecoverer -> trades.raw.DLT
     */
    @KafkaListener(topics = "${tradeflow.topics.raw}")
    public void onTrade(TradeEvent trade) {
        BigDecimal notional = trade.price().multiply(BigDecimal.valueOf(trade.quantity()));
        Optional<String> rejectReason = validator.rejectReason(trade, notional);

        TradeDocument document = new TradeDocument(
                trade.tradeId(),
                trade.account(),
                trade.symbol(),
                trade.side(),
                trade.quantity(),
                trade.price(),
                notional,
                rejectReason.isPresent() ? TradeStatus.REJECTED : TradeStatus.ACCEPTED,
                rejectReason.orElse(null),
                trade.receivedAt(),
                Instant.now(clock)
        );

        repository.save(document);   // upsert by _id = idempotent
        log.info("Trade {} {} (account={}, notional={})", trade.tradeId(), document.status(),
                trade.account(), notional.toPlainString());
    }
}
