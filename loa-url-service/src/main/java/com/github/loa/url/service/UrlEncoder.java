package com.github.loa.url.service;

import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/**
 * This service is responsible for encoding existing {@link URL} instances to valid
 * <a href="https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier">resource identifiers</a>.
 */
@Service
public class UrlEncoder {

    /**
     * Encodes the provided URL to a valid
     * <a href="https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier">resource identifier</a> and return
     * the new identifier as an URL.
     *
     * @param url the url to encode
     * @return the encoded url
     */
    public Optional<URL> encode(final URL url) {
        try {
            final URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());

            return Optional.of(new URL(uri.toASCIIString()));
        } catch (URISyntaxException | MalformedURLException e) {
            // The constructor of java.net.URL doesn't always validate the passed url correctly so an exception
            // could happen here.
            return Optional.empty();
        }
    }
}
