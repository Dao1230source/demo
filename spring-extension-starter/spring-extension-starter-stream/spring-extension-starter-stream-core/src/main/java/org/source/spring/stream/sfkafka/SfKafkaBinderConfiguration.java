package org.source.spring.stream.sfkafka;

import org.source.spring.stream.StreamBinder;
import org.source.spring.stream.StreamBinderProperties;
import org.source.spring.stream.StreamProvisioningProvider;
import org.source.spring.stream.converter.StringMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
public class SfKafkaBinderConfiguration {
    @Order(-1)
    @ConditionalOnMissingBean
    @Bean
    public MessageConverter sfKafkaMessageConverter() {
        return new StringMessageConverter();
    }

    @Bean
    public StreamProvisioningProvider<SfKafkaConsumerProcessor, SfKafkaProducerProcessor> sfKafkaProvisioningProvider() {
        return new StreamProvisioningProvider<>();
    }

    @Bean
    public StreamBinderProperties<SfKafkaConsumerProcessor, SfKafkaProducerProcessor> sfKafkaBinderProperties(SfKafkaPropertiesHandler propertiesParser) {
        return new StreamBinderProperties<>(propertiesParser);
    }

    @Bean
    public StreamBinder<SfKafkaConsumerProcessor, SfKafkaProducerProcessor> sfKafkaBinder(
            StreamProvisioningProvider<SfKafkaConsumerProcessor, SfKafkaProducerProcessor> sfKafkaProvisioningProvider,
            StreamBinderProperties<SfKafkaConsumerProcessor, SfKafkaProducerProcessor> sfKafkaBinderProperties) {
        return new StreamBinder<>(null, sfKafkaProvisioningProvider, sfKafkaBinderProperties);
    }
}