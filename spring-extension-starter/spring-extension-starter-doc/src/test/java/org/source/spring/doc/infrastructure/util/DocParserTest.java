package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocParser测试
 * <p>
 * 测试 DocParser 的基本功能
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocParserTest {

    @Test
    void testDocParserConstructorWithNullProcessor() {
        assertThrows(IllegalStateException.class, () -> {
            DocParser parser = new DocParser(null);
            parser.parseDirectory("/tmp");
        }, "Should throw IllegalStateException when objectProcessor is null");
    }

    @Test
    void testDocParserGetObjectProcessor() {
        DocParser parser = new DocParser(null);
        assertNull(parser.getObjectProcessor());
    }
}