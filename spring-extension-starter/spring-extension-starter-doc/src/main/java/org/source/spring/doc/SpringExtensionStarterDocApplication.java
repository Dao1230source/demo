package org.source.spring.doc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class SpringExtensionStarterDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringExtensionStarterDocApplication.class, args);
    }

}
