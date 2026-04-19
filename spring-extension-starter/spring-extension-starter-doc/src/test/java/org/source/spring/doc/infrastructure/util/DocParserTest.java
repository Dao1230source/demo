package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.source.spring.doc.infrastructure.config.DocConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocParser测试
 */
public class DocParserTest {

    @Test
    void testDocParserConstructorWithNullProcessor() {
        DocParser parser = new DocParser(null);
        assertNull(parser.getObjectProcessor());
        assertNotNull(parser.getDocConfig());
    }

    @Test
    void testDocParserConstructorWithConfig() {
        DocConfig config = new DocConfig();
        config.setEnableParallel(false);
        DocParser parser = new DocParser(null, config);
        assertNotNull(parser.getDocConfig());
        assertFalse(parser.getDocConfig().isEnableParallel());
    }

    @Test
    void testDocParserParseDirectoryWithNullProcessor() {
        DocParser parser = new DocParser(null);
        assertThrows(IllegalStateException.class, () -> {
            parser.parseDirectory("/tmp");
        });
    }

    @Test
    void testDocParserGetObjectProcessor() {
        DocParser parser = new DocParser(null);
        assertNull(parser.getObjectProcessor());
    }
}