package com.github.loa.vault.service.location.s3.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "s3")
public class S3Configuration {

    private final S3ConfigurationProperties s3ConfigurationProperties;

    @Bean
    public S3Client s3Client(final StaticCredentialsProvider staticCredentialsProvider) {
        return S3Client.builder()
                .endpointOverride(s3ConfigurationProperties.getEndpointLocation())
                .region(Region.of(s3ConfigurationProperties.getRegion()))
                .credentialsProvider(staticCredentialsProvider)
                .build();
    }

    @Bean
    public StaticCredentialsProvider staticCredentialsProvider(final AwsCredentials awsCredentials) {
        return StaticCredentialsProvider.create(awsCredentials);
    }

    @Bean
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create(s3ConfigurationProperties.getAccessKey(),
                s3ConfigurationProperties.getSecretKey());
    }
}
