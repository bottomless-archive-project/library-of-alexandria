package com.github.bottomlessarchive.loa.downloader.configuration;

import com.github.bottomlessarchive.loa.downloader.configuration.ssl.TrustAllTrustManager;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class is responsible to create the web client that is used to download documents from the internet.
 */
@Configuration
public class DownloaderClientConfiguration {

    @Bean
    public OkHttpClient downloaderClient(
            @Qualifier("trustAllSSLContext") final SSLContext trustAllSSLContext,
            @Qualifier("trustAllTrustManager") final TrustManager[] trustAllTrustManager) {
        return new OkHttpClient.Builder()
                // We don't care about hostname verification when download files. See #441 for more info.
                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), (X509TrustManager) trustAllTrustManager[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Bean
    @SneakyThrows
    protected SSLContext trustAllSSLContext(@Qualifier("trustAllTrustManager") final TrustManager[] trustAllTrustManager) {
        final SSLContext sslContext = SSLContext.getInstance("SSL");

        // Disabling SSL trust management & checking. See #441 for more info.
        sslContext.init(null, trustAllTrustManager, new java.security.SecureRandom());

        return sslContext;
    }

    @Bean
    protected TrustManager[] trustAllTrustManager(final TrustAllTrustManager trustAllTrustManager) {
        return new TrustManager[]{trustAllTrustManager};
    }
}
