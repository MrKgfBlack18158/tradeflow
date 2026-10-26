package com.tradeflow.query;

import java.math.BigDecimal;

/** Net holding of one symbol for one account. Positive netQuantity = long, negative = short. */
public record Position(String symbol, long netQuantity, BigDecimal buyNotional, BigDecimal sellNotional) {
}
