package com.github.loa.indexer.service.index.base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Base64EncoderTest {

    private Base64Encoder base64Encoder;

    @BeforeEach
    public void setup() {
        base64Encoder = new Base64Encoder();
    }

    @Test
    void testEncode() {
        final String result = base64Encoder.encode(new ByteArrayInputStream("This IS a TEST string!? :O"
                .getBytes(StandardCharsets.UTF_8)));

        assertEquals(result, "VGhpcyBJUyBhIFRFU1Qgc3RyaW5nIT8gOk8=");
    }
}