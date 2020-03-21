package com.github.loa.downloader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * The runner class of the downloader application.
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.github.loa", exclude = ClientHttpConnectorAutoConfiguration.class)
public class LibraryDownloaderApplication {

    private static final String[] UNUSABLE_LOGGING = new String[]{
            "org.apache.pdfbox.util.PDFStreamEngine",
            "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
            "org.apache.pdfbox.pdmodel.font.PDFont",
            "org.apache.pdfbox.pdmodel.font.FontManager",
            "org.apache.pdfbox.pdfparser.PDFObjectStreamParser",
            "org.apache.fontbox.ttf.GlyphSubstitutionTable"
    };

    public static void main(final String[] args) {
        disableUnusableLogging();

        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }

    private static void disableUnusableLogging() {
        Arrays.stream(UNUSABLE_LOGGING)
                .forEach(logger -> LogManager.getLogger(logger).isEnabled(Level.OFF));
    }
}
