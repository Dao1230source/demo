package org.source.spring.stream;

import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.source.utility.utils.Jsons;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequestMapping("/stream/sfKafka")
@RestController
public class SfKafkaStreamController {


    @Data
    public static class TestParam<T> {
        private String producerChannelName;
        private T data;
    }

    @Resource
    private StreamBridge streamBridge;

    @RequestMapping("/test")
    public void test(@RequestBody TestParam<Object> testParam) {
        streamBridge.send(testParam.getProducerChannelName(), testParam.getData());
    }

    @NoArgsConstructor
    @Data
    public static class TestData {
        private String key;
        private String value;
    }

    @Bean
    public Consumer<List<TestData>> testIn() {
        return list -> {
            log.info("list:{}", list);
        };
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[]{123, 34, 107, 101, 121, 34, 58, 34, 107, 101, 121, 34, 44, 34, 118, 97, 108, 117, 101, 34, 58, 34, 118, 97, 108, 117, 101, 34, 125};
        // Object obj = Jsons.obj(bytes, Jsons.getJavaType(List.class, TestData.class));
        Object obj = Jsons.obj(bytes, Jsons.getJavaType(TestData.class));
        System.out.println(obj);
    }
}