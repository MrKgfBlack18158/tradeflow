package com.tradeflow.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradePublisher {

    private static final Logger log = LoggerFactory.getLogger(TradePublisher.class);

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    private final String topic;

    public TradePublisher(KafkaTemplate<String, TradeEvent> kafkaTemplate,
                          @Value("${tradeflow.topics.raw}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Key = account. Kafka sends all messages with the same key to the same partition,
     * so trades for one account are processed in the order they arrived.
     */
    public void publish(TradeEvent event) {
        kafkaTemplate.send(topic, event.account(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish trade {}", event.tradeId(), ex);
                    } else {
                        log.info("Published trade {} to {}-{}@{}", event.tradeId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
