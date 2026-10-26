package com.tradeflow.processor;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/** Business rules a trade must pass. Returns the reject reason, or empty if the trade is fine. */
@Component
public class TradeValidator {

    private final RiskLimits limits;

    public TradeValidator(RiskLimits limits) {
        this.limits = limits;
    }

    public Optional<String> rejectReason(TradeEvent trade, BigDecimal notional) {
        if (!"BUY".equals(trade.side()) && !"SELL".equals(trade.side())) {
            return Optional.of("Unknown side: " + trade.side());
        }
        if (trade.quantity() > limits.maxQuantity()) {
            return Optional.of("Quantity " + trade.quantity() + " exceeds limit " + limits.maxQuantity());
        }
        if (notional.compareTo(limits.maxNotional()) > 0) {
            return Optional.of("Notional " + notional.toPlainString() + " exceeds limit " + limits.maxNotional().toPlainString());
        }
        return Optional.empty();
    }
}
