package com.github.bottomlessarchive.loa.beacon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The runner class of the beacon application.
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.github.bottomlessarchive.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.bottomlessarchive.loa")
public class LibraryBeaconApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryBeaconApplication.class, args);
    }
}
