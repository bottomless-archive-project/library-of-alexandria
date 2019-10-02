package com.github.loa.administrator.command.document.validator.domain;

import com.github.loa.document.service.domain.DocumentEntity;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.math3.util.Precision;

@Getter
@Builder
public class ValidatorDocument {

    private final DocumentEntity documentEntity;
    private final byte[] documentContents;

    /**
     * Return the document's size in megabytes rounded to precision 2.
     *
     * @return the document's size in megabytes
     */
    public double getSize() {
        return Precision.round((double) documentContents.length / (double) (1024L * 1024L), 2);
    }
}
