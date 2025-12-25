package org.source.spring.stream.sfkafka;

import com.sf.kafka.api.produce.ProduceConfig;
import com.sf.kafka.api.produce.ProduceOptionalConfig;
import lombok.Data;
import org.source.spring.stream.Producer;
import org.source.spring.stream.template.ProducerProcessor;
import org.springframework.cloud.stream.provisioning.ProvisioningException;

@Data
public class SfKafkaProducerProcessor implements ProducerProcessor {
    /*sf-kafka生产者配置*/
    private String monitorUrl;
    private String clusterName;
    private String topic;
    private String topicTokens;
    private int poolSize = 4;
    private int ack = 1;

    @Override
    public Producer createProducer() throws ProvisioningException {
        ProduceConfig produceConfig = new ProduceConfig(poolSize, monitorUrl, clusterName, topicTokens);
        ProduceOptionalConfig optionalConfig = new ProduceOptionalConfig();
        if (this.ack > 0) {
            optionalConfig.setRequestRequiredAck(ProduceOptionalConfig.RequestRequiredAck.LEADER_REPLICA);
        } else if (this.ack < 0) {
            optionalConfig.setRequestRequiredAck(ProduceOptionalConfig.RequestRequiredAck.ALL_REPLICA);
        } else {
            optionalConfig.setRequestRequiredAck(ProduceOptionalConfig.RequestRequiredAck.NEVER_WAIT);
        }
        return new SfKafkaProducer(this.topic, produceConfig, optionalConfig);
    }
}