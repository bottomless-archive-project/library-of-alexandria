package com.github.bottomlessarchive.loa.document.service.location;

import com.github.bottomlessarchive.loa.location.domain.link.StringLink;
import com.github.bottomlessarchive.loa.location.service.validation.DocumentLocationValidator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.validation.extension.FileExtensionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentLocationValidatorTest {

    private static final String EMPTY_LINK = "";

    @Mock
    private FileExtensionValidator fileExtensionValidator;

    @InjectMocks
    private DocumentLocationValidator underTest;

    @Test
    void testValidDocumentLocation() {
        when(fileExtensionValidator.isValidPathWithExtension("/test.epub"))
                .thenReturn(true);

        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(
                                StringLink.builder()
                                        .link("http://www.example.com/test.epub?queryparam=value")
                                        .build()
                        )
                        .build()
        );

        assertThat(result)
                .isTrue();
        verify(fileExtensionValidator).isValidPathWithExtension("/test.epub");
    }

    @Test
    void testInvalidDocumentLocationWithNoDomainExtension() {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(
                                StringLink.builder()
                                        .link("helloWorld")
                                        .build()
                        )
                        .build()
        );

        assertThat(result)
                .isFalse();
    }

    @Test
    void testInvalidDocumentLocationBadDomainLocation() {
        when(fileExtensionValidator.isValidPathWithExtension(any()))
                .thenReturn(false);

        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(
                                StringLink.builder()
                                        .link("http://askdoctorkcom-back-strengthening-exercises-37d5.pdf/")
                                        .build()
                        )
                        .build()
        );

        assertThat(result)
                .isFalse();
    }

    // Empty string makes JUnit throw an invalid CSV error, so we need to test it in its own method
    @Test
    void testEmptyDocumentLocation() {
        final boolean result = underTest.validDocumentLocation(
                DocumentLocation.builder()
                        .location(StringLink.builder()
                                .link(EMPTY_LINK)
                                .build()
                        )
                        .build()
        );

        assertThat(result)
                .isFalse();
    }
}
