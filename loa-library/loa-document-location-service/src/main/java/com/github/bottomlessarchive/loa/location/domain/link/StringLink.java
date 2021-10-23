package com.github.bottomlessarchive.loa.location.domain.link;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
public class StringLink implements Link {

    private final URL url;

    @Builder
    public StringLink(final String link) {
        URL tempUrl = null;
        try {
            tempUrl = new URL(link);
        } catch (final MalformedURLException e) {
            log.debug("Unable to parse url with location: {}.", link, e);
        }
        url = tempUrl;
    }

    @Override
    public Optional<URL> toUrl() {
        return Optional.ofNullable(url);
    }

    @Override
    public boolean isValid() {
        // The empty string is validated by the URL validator
        return url != null;
    }
}
