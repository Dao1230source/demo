package org.source.spring.stream.sfkafka;

import com.sf.kafka.api.consume.IStringMessageConsumeListener;
import com.sf.kafka.api.consume.KafkaConsumeRetryException;
import org.source.spring.stream.template.AbstractListener;

import java.util.List;

public class SfKafkaListener extends AbstractListener<String> implements IStringMessageConsumeListener {

    @Override
    public void onMessage(List<String> list) throws KafkaConsumeRetryException {
        this.processMessage(list);
    }
}