package com.tradeflow.query;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PositionCalculatorTest {

    private final PositionCalculator calculator = new PositionCalculator();

    @Test
    void nettingBuysAndSellsPerSymbol() {
        List<Position> positions = calculator.positions(List.of(
                trade("INFY", "BUY", 100, "152000"),
                trade("INFY", "SELL", 40, "61000"),
                trade("TCS", "BUY", 10, "41000")
        ));

        assertThat(positions).hasSize(2);
        Position infy = positions.get(0);
        assertThat(infy.symbol()).isEqualTo("INFY");
        assertThat(infy.netQuantity()).isEqualTo(60);
        assertThat(infy.buyNotional()).isEqualByComparingTo("152000");
        assertThat(infy.sellNotional()).isEqualByComparingTo("61000");
    }

    @Test
    void noTradesMeansNoPositions() {
        assertThat(calculator.positions(List.of())).isEmpty();
    }

    private static TradeView trade(String symbol, String side, long qty, String notional) {
        return new TradeView("id-" + symbol + side, "ACC-1", symbol, side, qty, BigDecimal.ONE,
                new BigDecimal(notional), "ACCEPTED", null, Instant.now(), Instant.now());
    }
}
