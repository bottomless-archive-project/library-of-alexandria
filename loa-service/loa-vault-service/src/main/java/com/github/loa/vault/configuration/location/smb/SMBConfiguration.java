package com.github.loa.vault.configuration.location.smb;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SMBConfiguration {

    @Bean
    public SMBClient smbClient() {
        final SmbConfig config = SmbConfig.builder()
                .withTimeout(120, TimeUnit.SECONDS)
                .withSoTimeout(180, TimeUnit.SECONDS)
                .build();

        return new SMBClient(config);
    }
}
