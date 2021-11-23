package com.github.bottomlessarchive.loa.url.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlEncoderTest {

    private final UrlEncoder underTest = new UrlEncoder();

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/?test=Hello world,http://www.example.com/?test=Hello%20world",
                    "http://www.example.com/?test=ŐÚőúŰÜűü,http://www.example.com/?test=%C5%90%C3%9A%C5%91%C3%BA%C5%B0%C3%9C%C5%B1%C3%BC",
                    "http://www.example.com/?test=random word £500 bank $,http://www.example.com/?test=random%20word%20%C2%A3500%20bank%20$",
                    "http://www.aquincum.hu/wp-content/uploads/2015/06/Aquincumi-F%C3%BCzetek_14_2008.pdf,http://www.aquincum.hu/wp-content/uploads/2015/06/Aquincumi-F%C3%BCzetek_14_2008.pdf"
            }
    )
    void testEncodeWhenUsingValidUrls(final String urlToEncode, final String expected) throws MalformedURLException {
        final Mono<URL> result = underTest.encode(new URL(urlToEncode));

        StepVerifier.create(result)
                .consumeNextWith(resultValue -> assertEquals(expected, resultValue.toString()))
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://промкаталог.рф/PublicDocuments/05-0211-00.pdf"
            }
    )
    void testEncodeWhenUsingInvalidUrls(final String urlToEncode) throws MalformedURLException {
        final Mono<URL> result = underTest.encode(new URL(urlToEncode));

        StepVerifier.create(result)
                .verifyComplete();
    }
}
