package com.github.bottomlessarchive.loa.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeCalculatorTest {

    private final DocumentTypeCalculator underTest = new DocumentTypeCalculator();

    @ParameterizedTest
    @CsvSource({
            "http://lcoalhost/123.pdf,PDF",
            "http://lcoalhost/123.doc,DOC",
            "http://lcoalhost/123.docx,DOCX",
            "http://lcoalhost/123.ppt,PPT",
            "http://lcoalhost/123.pptx,PPTX",
            "http://lcoalhost/123.xls,XLS",
            "http://lcoalhost/123.xlsx,XLSX",
            "http://lcoalhost/123.rtf,RTF",
            "http://lcoalhost/123.mobi,MOBI",
            "http://lcoalhost/123.epub,EPUB",
            "http://lcoalhost/123.fb2,FB2"
    })
    void testCalculateForVariousTypes(final String url, final String expectedDocumentType) throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(new URL(url));

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.valueOf(expectedDocumentType));
    }

    @Test
    void testCalculateForUppercaseUrl() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(new URL("http://lcoalhost/123.PDF"));

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.PDF);
    }

    @Test
    void testCalculateForZippedFB2() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(new URL("http://lcoalhost/123.fb2.zip"));

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.FB2);
    }

    @Test
    void testCalculateForUppercaseZippedFB2Url() throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(new URL("http://lcoalhost/123.FB2.zip"));

        assertThat(result)
                .isPresent()
                .hasValue(DocumentType.FB2);
    }

    @ParameterizedTest
    @CsvSource({
            "http://lcoalhost/",
            "http://lcoalhost/123",
            "http://lcoalhost/123.exe"
    })
    void testCalculateForVariousTypes(final String url) throws MalformedURLException {
        final Optional<DocumentType> result = underTest.calculate(new URL(url));

        assertThat(result)
                .isEmpty();
    }
}