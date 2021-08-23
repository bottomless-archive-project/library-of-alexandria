package com.github.loa.web.view.document.configuration;

import com.mortennobel.imagescaling.ResampleOp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentRenderingConfiguration {

    @Bean
    public ResampleOp resampleOp() {
        return new ResampleOp(248, 350);
    }
}
