package com.github.bottomlessarchive.loa.document.service;

import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeCalculatorTest {

    private final DocumentTypeCalculator underTest = new DocumentTypeCalculator();

    @ParameterizedTest
    @CsvSource({
            "http://localhost/123.pdf,PDF",
            "http://localhost/123.doc,DOC",
            "http://localhost/123.docx,DOCX",
            "http://localhost/123.ppt,PPT",
            "http://localhost/123.pptx,PPTX",
            "http://localhost/123.xls,XLS",
            "http://localhost/123.xlsx,XLSX",
            "http://localhost/123.rtf,RTF",
            "http://localhost/123.mobi,MOBI",
            "http://localhost/123.epub,EPUB",
            "http://localhost/123.fb2,FB2",
            "http://localhost/123.txt,TXT"
    })
    void testCalculateForVariousTypes(final String url, final String expectedDocumentType) throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(URI.create(url).toURL());

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.valueOf(expectedDocumentType));
    }

    @Test
    void testCalculateForUppercaseUrl() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(URI.create("http://localhost/123.PDF").toURL());

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.PDF);
    }

    @Test
    void testCalculateForZippedFB2() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(URI.create("http://localhost/123.fb2.zip").toURL());

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.FB2);
    }

    @Test
    void testCalculateForUppercaseZippedFB2Url() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(URI.create("http://localhost/123.FB2.zip").toURL());

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.FB2);
    }

    @ParameterizedTest
    @CsvSource({
            "http://localhost/",
            "http://localhost/123",
            "http://localhost/123.exe"
    })
    void testCalculateForVariousTypes(final String url) throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(URI.create(url).toURL());

        assertThat(result)
                .isEmpty();
    }
}
