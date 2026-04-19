package org.source.spring.doc.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档模块配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "doc")
public class DocConfig {

    private List<String> scanPackages = new ArrayList<>();
    private List<String> excludePackages = new ArrayList<>();
    private List<String> fileExtensions = List.of(".java");
    private boolean enableParallel = true;
    private int parallelThreads = Runtime.getRuntime().availableProcessors();
    private boolean generateReport = true;
    private boolean parseValidationAnnotations = true;
    private boolean parseSpringAnnotations = true;
    private boolean parseFeignAnnotations = false;
    private boolean parseMyBatisAnnotations = false;
}