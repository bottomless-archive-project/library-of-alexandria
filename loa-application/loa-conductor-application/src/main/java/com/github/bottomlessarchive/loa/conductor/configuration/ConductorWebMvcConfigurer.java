package com.github.bottomlessarchive.loa.conductor.configuration;

import com.github.bottomlessarchive.loa.conductor.view.converter.StringToApplicationTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConductorWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(new StringToApplicationTypeConverter());
    }
}
