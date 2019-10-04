package com.github.loa.administrator.command.document.validator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.command.document-validator")
public class DocumentValidatorConfigurationProperties {

    private int parallelismLevel = 25;
    private String collectorFile;
}
