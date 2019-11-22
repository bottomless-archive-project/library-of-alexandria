package com.github.loa.parser.configuration;

import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentParserConfiguration {

    @Bean
    public Parser documentParser() {
        return new AutoDetectParser();
    }

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllBuiltInLanguages()
                .build();
    }
}
