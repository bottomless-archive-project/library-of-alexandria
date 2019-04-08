package com.github.loa.url.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlEncoderTest {

    private UrlEncoder urlEncoder;

    @BeforeEach
    private void setup() {
        urlEncoder = new UrlEncoder();
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/?test=Hello world,http://www.example.com/?test=Hello%20world",
                    "http://www.example.com/?test=ŐÚőúŰÜűü,http://www.example.com/?test=%C5%90%C3%9A%C5%91%C3%BA%C5%B0%C3%9C%C5%B1%C3%BC",
                    "http://www.example.com/?test=random word £500 bank $,http://www.example.com/?test=random%20word%20%C2%A3500%20bank%20$"
            }
    )
    void testEncodeWhenUsingValidUrls(final String urlToEncode, final String expected) throws MalformedURLException {
        final URL encoded = urlEncoder.encode(new URL(urlToEncode));

        assertEquals(expected, encoded.toString());
    }
}