package com.github.bottomlessarchive.loa.administrator.configuration;

import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StageConfiguration {

    private final AdministratorConfigurationProperties administratorConfigurationProperties;

    @Bean
    public StageLocationFactory stageLocationFactory() {
        return new StageLocationFactory(administratorConfigurationProperties.stagingDirectory());
    }
}
