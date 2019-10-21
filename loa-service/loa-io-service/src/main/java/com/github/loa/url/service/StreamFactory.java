package com.github.loa.url.service;

import org.davidmoten.io.extras.IOUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class StreamFactory {

    public InputStream openGZIPLocation(final String location) throws IOException {
        return IOUtil.gunzip(new URL(location).openStream());
    }
}
