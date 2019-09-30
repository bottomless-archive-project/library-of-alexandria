package com.github.loa.administrator.command.document.pagecount;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.command.page-count-parser")
public class PageCountParserConfigurationProperties {

    private int parallelismLevel = 25;
}
