package com.tradeflow.query;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TradeQueryController {

    private final TradeViewRepository repository;
    private final PositionCalculator calculator;

    public TradeQueryController(TradeViewRepository repository, PositionCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    /** 404 until the processor has stored it: the pipeline is asynchronous. */
    @GetMapping("/trades/{tradeId}")
    public ResponseEntity<TradeView> trade(@PathVariable String tradeId) {
        return ResponseEntity.of(repository.findById(tradeId));
    }

    @GetMapping("/accounts/{account}/trades")
    public List<TradeView> trades(@PathVariable String account) {
        return repository.findByAccountOrderByReceivedAtDesc(account);
    }

    @GetMapping("/accounts/{account}/positions")
    public List<Position> positions(@PathVariable String account) {
        return calculator.positions(repository.findByAccountAndStatus(account, "ACCEPTED"));
    }
}
