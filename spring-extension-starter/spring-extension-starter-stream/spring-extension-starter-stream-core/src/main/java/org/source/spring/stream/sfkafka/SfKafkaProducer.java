package org.source.spring.stream.sfkafka;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sf.kafka.api.produce.KeyedString;
import com.sf.kafka.api.produce.ProduceConfig;
import com.sf.kafka.api.produce.ProduceOptionalConfig;
import com.sf.kafka.api.produce.ProducerPool;
import org.source.spring.stream.template.KafkaProducer;
import org.source.utility.enums.BaseExceptionEnum;
import org.source.utility.utils.Jsons;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;

public class SfKafkaProducer extends ProducerPool implements KafkaProducer<String, String> {
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
    public void send(Message<?> message) {
        String payload = (String) message.getPayload();
        try (JsonParser jsonParser = Jsons.getInstance().createParser(payload)) {
            JsonToken jsonToken = jsonParser.nextToken();
            String key = this.getKey(message);
            if (jsonToken == JsonToken.START_OBJECT) {
                if (StringUtils.hasText(key)) {
                    this.sendKeyedString(this.getTopicName(), new KeyedString(key, payload));
                } else {
                    this.sendString(this.getTopicName(), payload);
                }
            } else {
                if (StringUtils.hasText(key)) {
                    List<HashMap<?, ?>> mapList = Jsons.obj(payload, new TypeReference<>() {
                    });
                    List<KeyedString> list = mapList.stream().map(k -> new KeyedString(key, Jsons.str(k))).toList();
                    this.batchSendKeyedString(this.getTopicName(), list);
                } else {
                    List<HashMap<?, ?>> mapList = Jsons.obj(payload, new TypeReference<>() {
                    });
                    List<String> list = mapList.stream().map(Jsons::str).toList();
                    this.batchSendString(this.getTopicName(), list);
                }
            }
        } catch (Exception e) {
            throw BaseExceptionEnum.JSON_STRING_2_OBJECT_EXCEPTION.except(e);
        }
    }
}