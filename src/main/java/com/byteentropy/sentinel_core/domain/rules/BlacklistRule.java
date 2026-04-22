package com.byteentropy.sentinel_core.domain.rules;

import com.byteentropy.sentinel_core.domain.model.Transaction;
import com.byteentropy.sentinel_core.infra.config.FraudProperties;
import com.byteentropy.sentinel_core.infra.config.KafkaStreamsConfig;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlacklistRule implements FraudRule {

    private final FraudProperties props;
    private final StreamsBuilderFactoryBean factoryBean;
    
    // Bridge for TopologyTestDriver
    @Setter 
    private ReadOnlyKeyValueStore<String, String> testStore;

    @Override
    public String getName() { return "Global User Blacklist"; }

    @Override
    public int getWeight() { return props.getWeights().getBlacklistRule(); }

    @Override
    public boolean evaluate(Transaction tx) {
        if (tx.getUserId() == null) return false;
        try {
            // Use testStore if injected by test, otherwise use live Kafka store
            ReadOnlyKeyValueStore<String, String> store = (testStore != null) ? testStore : 
                factoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(
                        KafkaStreamsConfig.BLACKLIST_STORE, 
                        QueryableStoreTypes.keyValueStore()
                ));

            return store.get(tx.getUserId()) != null;
        } catch (Exception e) {
            return false;
        }
    }
}