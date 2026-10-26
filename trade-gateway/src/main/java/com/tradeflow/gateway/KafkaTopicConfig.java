package com.tradeflow.gateway;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Creates the topic on startup if it doesn't exist. 3 partitions = up to 3 consumers in parallel. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic tradesRawTopic(@Value("${tradeflow.topics.raw}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
