package com.github.bottomlessarchive.loa.conductor.view.converter;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import org.springframework.core.convert.converter.Converter;

import java.util.Locale;

public class StringToApplicationTypeConverter implements Converter<String, ApplicationType> {

    @Override
    public ApplicationType convert(final String source) {
        return ApplicationType.valueOf(source.replaceAll("-", "_").toUpperCase(Locale.ENGLISH));
    }
}
