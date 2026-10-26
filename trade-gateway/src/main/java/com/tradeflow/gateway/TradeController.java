package com.tradeflow.gateway;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradePublisher publisher;

    public TradeController(TradePublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Returns 202 Accepted, not 201 Created: the trade is queued for processing,
     * not yet stored. The client uses tradeId to look it up in trade-query.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TradeAccepted submit(@Valid @RequestBody TradeRequest request) {
        TradeEvent event = TradeEvent.from(request);
        publisher.publish(event);
        return new TradeAccepted(event.tradeId(), "ACCEPTED");
    }

    public record TradeAccepted(String tradeId, String status) {
    }
}
