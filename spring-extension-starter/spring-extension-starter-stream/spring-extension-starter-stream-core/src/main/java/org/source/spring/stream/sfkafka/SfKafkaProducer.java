package org.source.spring.stream.sfkafka;

import com.sf.kafka.api.produce.KeyedBytes;
import com.sf.kafka.api.produce.ProduceConfig;
import com.sf.kafka.api.produce.ProduceOptionalConfig;
import com.sf.kafka.api.produce.ProducerPool;
import org.source.spring.stream.template.KafkaProducer;
import org.springframework.messaging.Message;

public class SfKafkaProducer extends ProducerPool implements KafkaProducer<String, byte[]> {
    private final String topic;

    public SfKafkaProducer(String topic, ProduceConfig config, ProduceOptionalConfig optionalConfig) {
        super(config, optionalConfig);
        this.topic = topic;
    }

    @Override
    public String getTopicName() {
        return this.topic;
    }

    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    @Override
    public byte[] getValue(Message<?> message) {
        return (byte[]) message.getPayload();
    }

    @Override
    public void send(String topic, String key, byte[] bytes) {
        KeyedBytes keyedBytes = new KeyedBytes(key, bytes);
        super.sendKeyedBytes(topic, keyedBytes);
    }

    @Override
    public void send(String topic, byte[] bytes) {
        super.sendBytes(topic, bytes);
    }
}