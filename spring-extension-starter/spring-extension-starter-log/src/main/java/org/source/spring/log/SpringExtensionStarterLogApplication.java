package org.source.spring.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableExtendedLog
@SpringBootApplication
public class SpringExtensionStarterLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringExtensionStarterLogApplication.class, args);
    }

}
