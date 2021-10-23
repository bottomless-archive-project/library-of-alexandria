package com.github.loa.location.domain.link;

import java.net.URL;
import java.util.Optional;

public interface Link {

    Optional<URL> toUrl();

    boolean isValid();
}
