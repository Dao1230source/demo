package org.source.spring.stream.sfkafka;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.source.spring.stream.template.AbstractPropertiesHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Validated
@Data
@Component
@ConfigurationProperties("spring.cloud.stream")
public class SfKafkaPropertiesHandler extends AbstractPropertiesHandler<SfKafkaConsumerProcessor, SfKafkaProducerProcessor,
        SfKafkaPropertiesHandler.SfKafkaConsumer, SfKafkaPropertiesHandler.SfKafkaProducer,
        SfKafkaPropertiesHandler.SfKafkaSystem> {

    private Map<String, SfKafkaSystem> systems;

    @Data
    public static class SfKafkaSystem implements SystemProperty<SfKafkaConsumer, SfKafkaProducer> {
        private boolean isEnable = true;
        @NotEmpty(message = "monitorUrl can not be empty")
        private String monitorUrl;
        private String clusterName;

        private Map<String, SfKafkaProducer> producers;
        private Map<String, SfKafkaConsumer> consumers;
    }

    @Data
    public static class SfKafkaProducer implements ProducerProperty {
        private boolean isEnable = true;
        private String topic;
        private String topicTokens;
        @Nullable
        private Integer poolSize;
        @Nullable
        private Integer ack;
    }

    @Override
    protected SfKafkaProducerProcessor obtainProducer(String systemName, SfKafkaSystem system,
                                                      String producerName, SfKafkaProducer producer) {
        SfKafkaProducerProcessor producerProcessor = new SfKafkaProducerProcessor();
        producerProcessor.setMonitorUrl(system.getMonitorUrl());
        producerProcessor.setClusterName(system.getClusterName());
        producerProcessor.setTopic(producer.getTopic());
        producerProcessor.setTopicTokens(producer.getTopicTokens());
        producerProcessor.setPoolSize(Objects.requireNonNullElse(producer.getPoolSize(), 4));
        producerProcessor.setAck(Objects.requireNonNullElse(producer.getAck(), 1));
        return producerProcessor;
    }

    @Data
    public static class SfKafkaConsumer implements ConsumerProperty {
        private boolean isEnable = true;
        private String systemIdToken;
        private String topic;
        @Nullable
        private Integer messageConsumeThreadCount;
        @Nullable
        private Integer consumeMessageGroupSize;
    }

    @Override
    protected SfKafkaConsumerProcessor obtainConsumer(String systemName, SfKafkaSystem system,
                                                      String producerName, SfKafkaConsumer consumer) {
        SfKafkaConsumerProcessor consumerProcessor = new SfKafkaConsumerProcessor();
        consumerProcessor.setMonitorUrl(system.getMonitorUrl());
        consumerProcessor.setClusterName(system.getClusterName());
        consumerProcessor.setSystemIdToken(consumer.getSystemIdToken());
        consumerProcessor.setTopic(consumer.getTopic());
        consumerProcessor.setMessageConsumeThreadCount(Objects.requireNonNullElse(consumer.getMessageConsumeThreadCount(), 4));
        consumerProcessor.setConsumeMessageGroupSize(Objects.requireNonNullElse(consumer.getConsumeMessageGroupSize(), 20));
        return consumerProcessor;
    }
}