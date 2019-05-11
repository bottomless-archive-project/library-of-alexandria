package com.github.loa.migrator.command.compressor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.github.loa")
@ConditionalOnProperty("silent-compressor")
public class CompressorConfiguration {
}
