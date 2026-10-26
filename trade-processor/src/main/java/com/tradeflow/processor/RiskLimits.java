package com.tradeflow.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Bound from tradeflow.risk.* in application.yml, so limits change without a code change. */
@ConfigurationProperties(prefix = "tradeflow.risk")
public record RiskLimits(long maxQuantity, BigDecimal maxNotional) {
}
