package com.tradeflow.processor;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeRepository extends MongoRepository<TradeDocument, String> {
}
