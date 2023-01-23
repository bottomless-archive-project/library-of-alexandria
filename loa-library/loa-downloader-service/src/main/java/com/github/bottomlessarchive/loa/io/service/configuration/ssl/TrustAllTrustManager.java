package com.github.bottomlessarchive.loa.io.service.configuration.ssl;

import org.springframework.stereotype.Component;

import javax.net.ssl.X509TrustManager;

@Component
public class TrustAllTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
    }

    @Override
    public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[]{};
    }
}
