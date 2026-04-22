package com.byteentropy.sentinel_core.infra.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic blacklistTopic() {
        return TopicBuilder.name("blacklist-users")
                .partitions(1)
                .replicas(1)
                .compact() // Important for blacklists!
                .build();
    }

    @Bean
    public NewTopic inboundTopic() {
        return TopicBuilder.name("inbound-transactions")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic outboundTopic() {
        return TopicBuilder.name("fraud-decisions")
                .partitions(1)
                .replicas(1)
                .build();
    }
}