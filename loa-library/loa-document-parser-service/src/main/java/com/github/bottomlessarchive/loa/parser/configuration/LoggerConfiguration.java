package com.github.bottomlessarchive.loa.parser.configuration;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

//TODO: Review if this is still necessary
@Configuration
public class LoggerConfiguration {

    private static final String[] UNUSABLE_LOGGING = {
            "org.apache.pdfbox.util.PDFStreamEngine",
            "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
            "org.apache.pdfbox.pdmodel.font.PDFont",
            "org.apache.pdfbox.pdmodel.font.FontManager",
            "org.apache.pdfbox.pdfparser.PDFObjectStreamParser",
            "org.apache.fontbox.ttf.GlyphSubstitutionTable"
    };

    @PostConstruct
    public void disableUnusableLogging() {
        Arrays.stream(UNUSABLE_LOGGING)
                .forEach(logger -> LogManager.getLogger(logger).isEnabled(Level.OFF));
    }
}
