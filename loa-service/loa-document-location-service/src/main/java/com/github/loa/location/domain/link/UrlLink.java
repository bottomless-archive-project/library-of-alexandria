package com.github.loa.location.domain.link;

import lombok.Builder;

import java.net.URL;
import java.util.Optional;

@Builder
public class UrlLink implements Link {

    private final URL url;

    @Override
    public Optional<URL> toUrl() {
        return Optional.ofNullable(url);
    }

    @Override
    public boolean isValid() {
        return url != null;
    }
}
