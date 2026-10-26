package com.tradeflow.query;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Aggregates accepted trades into positions, in Java.
 * TODO (Wed/Thu session): replace with a MongoDB aggregation pipeline ($match -> $group)
 * so the database does the work, and compare the two approaches. Good interview talking point.
 */
@Component
public class PositionCalculator {

    public List<Position> positions(List<TradeView> acceptedTrades) {
        Map<String, Position> bySymbol = new TreeMap<>();
        for (TradeView trade : acceptedTrades) {
            boolean buy = "BUY".equals(trade.side());
            Position current = bySymbol.getOrDefault(trade.symbol(),
                    new Position(trade.symbol(), 0, BigDecimal.ZERO, BigDecimal.ZERO));
            bySymbol.put(trade.symbol(), new Position(
                    trade.symbol(),
                    current.netQuantity() + (buy ? trade.quantity() : -trade.quantity()),
                    buy ? current.buyNotional().add(trade.notional()) : current.buyNotional(),
                    buy ? current.sellNotional() : current.sellNotional().add(trade.notional())
            ));
        }
        return bySymbol.values().stream()
                .sorted(Comparator.comparing(Position::symbol))
                .toList();
    }
}
