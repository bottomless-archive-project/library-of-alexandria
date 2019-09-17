package com.github.loa.administrator.command.document;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.command.document-validator")
public class DocumentValidatorConfigurationProperties {

    private int parallelismLevel = 25;
}
