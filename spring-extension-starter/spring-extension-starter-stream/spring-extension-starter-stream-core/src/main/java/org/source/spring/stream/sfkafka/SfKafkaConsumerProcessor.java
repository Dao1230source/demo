package org.source.spring.stream.sfkafka;

import com.sf.kafka.api.consume.ConsumeConfig;
import com.sf.kafka.api.consume.ConsumeOptionalConfig;
import com.sf.kafka.api.consume.KafkaConsumerRegister;
import com.sf.kafka.exception.KafkaException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.source.spring.stream.Listener;
import org.source.spring.stream.template.ConsumerProcessor;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

@Slf4j
@Data
public class SfKafkaConsumerProcessor implements ConsumerProcessor {
    /*sf-kafka消费者配置*/
    private String monitorUrl;
    private String clusterName;
    private String topic;
    private String systemIdToken;
    private int messageConsumeThreadCount = 4;
    private int consumeMessageGroupSize = 20;

    @Override
    public Listener createConsumer() throws ProvisioningException {
        ConsumeConfig pickupConsumeConfig = new ConsumeConfig(this.systemIdToken, this.monitorUrl,
                this.clusterName, this.topic, this.messageConsumeThreadCount);
        ConsumeOptionalConfig optionalConfig = new ConsumeOptionalConfig();
        optionalConfig.setMessageGroupSize(this.consumeMessageGroupSize);
        SfKafkaListener sfKafkaListener = new SfKafkaListener();
        try {
            boolean successful = KafkaConsumerRegister.registerStringConsumer(pickupConsumeConfig, sfKafkaListener, optionalConfig);
            if (successful) {
                log.info("sf-kafka register successfully，{}", this.topic);
            }
        } catch (KafkaException e) {
            log.error("throw exception where register sf-kafka consumer:{}", this.topic, e);
        }
        return sfKafkaListener;
    }
}