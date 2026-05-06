package org.source.spring.doc.infrastructure.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.SpringExtensionStarterDocApplication;
import org.source.spring.doc.domain.object.DocObjectProcessor;
import org.source.spring.doc.infrastructure.config.DocConfig;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DocParser测试
 */
@Slf4j
@SpringBootTest(classes = SpringExtensionStarterDocApplication.class)
public class DocParserTest {
    @Resource
    private DocObjectProcessor objectProcessor;
    @Resource
    private DocConfig docConfig;

    @Test
    void testDocParserConstructorWithProcessor() throws IOException {
        DocParser parser = new DocParser(objectProcessor, docConfig);
        parser.parse();
        assertNotNull(parser.getObjectProcessor());
        assertNotNull(parser.getDocConfig());
    }
}