package com.github.bottomlessarchive.loa.vault.configuration;

import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StageConfiguration {

    private final VaultConfigurationProperties vaultConfigurationProperties;

    @Bean
    public StageLocationFactory stageLocationFactory() {
        return new StageLocationFactory(vaultConfigurationProperties.stagingDirectory());
    }
}
