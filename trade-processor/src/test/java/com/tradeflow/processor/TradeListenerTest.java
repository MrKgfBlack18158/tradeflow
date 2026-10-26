package com.tradeflow.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Plain unit test: no Spring, no Kafka, no MongoDB. The repository is a Mockito mock. */
class TradeListenerTest {

    private TradeRepository repository;
    private TradeListener listener;

    @BeforeEach
    void setUp() {
        repository = mock(TradeRepository.class);
        RiskLimits limits = new RiskLimits(1_000_000, new BigDecimal("10000000"));
        Clock fixed = Clock.fixed(Instant.parse("2026-10-26T10:00:00Z"), ZoneOffset.UTC);
        listener = new TradeListener(repository, new TradeValidator(limits), Optional.of(fixed));
    }

    @Test
    void acceptsTradeWithinLimitsAndComputesNotional() {
        listener.onTrade(trade("BUY", 100, "1520.50"));

        TradeDocument saved = captureSaved();
        assertThat(saved.status()).isEqualTo(TradeStatus.ACCEPTED);
        assertThat(saved.notional()).isEqualByComparingTo("152050.00");
        assertThat(saved.rejectReason()).isNull();
    }

    @Test
    void rejectsTradeOverNotionalLimit() {
        listener.onTrade(trade("SELL", 10_000, "2000"));   // notional 20,000,000

        TradeDocument saved = captureSaved();
        assertThat(saved.status()).isEqualTo(TradeStatus.REJECTED);
        assertThat(saved.rejectReason()).contains("Notional");
    }

    @Test
    void rejectsUnknownSide() {
        listener.onTrade(trade("HOLD", 1, "10"));

        assertThat(captureSaved().status()).isEqualTo(TradeStatus.REJECTED);
    }

    private TradeDocument captureSaved() {
        ArgumentCaptor<TradeDocument> captor = ArgumentCaptor.forClass(TradeDocument.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private static TradeEvent trade(String side, long quantity, String price) {
        return new TradeEvent("t-1", "ACC-1", "INFY", side, quantity, new BigDecimal(price),
                Instant.parse("2026-10-26T09:59:59Z"));
    }
}
