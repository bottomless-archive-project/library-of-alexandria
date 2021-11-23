package com.github.bottomlessarchive.loa.url.service.encoder;

import io.mola.galimatias.GalimatiasParseException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This service is responsible for encoding existing {@link URL} instances to valid
 * <a href="https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier">resource identifiers</a>.
 */
@Service
public class UrlEncoder {

    /**
     * Encodes the provided URL to a valid
     * <a href="https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier">resource identifier</a> and return
     * the new identifier as a URL.
     *
     * @param url the url to encode
     * @return the encoded url
     */
    public Mono<URL> encode(final URL url) {
        try {
            // We need to further validate the URL because the java.net.URL's validation is inadequate.
            validateUrl(url);

            return Mono.just(encodeUrl(url));
        } catch (GalimatiasParseException | MalformedURLException | URISyntaxException e) {
            return Mono.empty();
        }
    }

    private void validateUrl(final URL url) throws URISyntaxException {
        // This will trigger an URISyntaxException. It is needed because the constructor of java.net.URL doesn't always validate the
        // passed url correctly.
        new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
    }

    private URL encodeUrl(final URL url) throws GalimatiasParseException, MalformedURLException {
        return io.mola.galimatias.URL.parse(url.toString()).toJavaURL();
    }
}
