package com.tradeflow.query;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeViewRepository extends MongoRepository<TradeView, String> {

    // Spring Data derives the MongoDB query from the method name.
    List<TradeView> findByAccountOrderByReceivedAtDesc(String account);

    List<TradeView> findByAccountAndStatus(String account, String status);
}
