package com.github.bottomlessarchive.loa.location.service.factory;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.url.service.encoder.UrlEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentLocationFactory {

    private final UrlEncoder urlEncoder;
    private final DocumentTypeCalculator documentTypeCalculator;
    private final DocumentLocationIdFactory documentLocationIdFactory;

    /**
     * Tries to construct a {@link DocumentLocation} from a provided {@link String} representation. If the conversion failed because the
     * link vas invalid, it will return an empty optional.
     *
     * @param link       the link to convert into a location
     * @param sourceName the source of the document location
     * @return the converted document location
     */
    public Optional<DocumentLocation> newDocumentLocation(final String link, final String sourceName) {
        return urlEncoder.encode(link)
                .flatMap(url -> documentTypeCalculator.calculate(url)
                        .map(type ->
                                DocumentLocation.builder()
                                        .id(documentLocationIdFactory.newDocumentLocationId(url))
                                        .location(url)
                                        .sourceName(sourceName)
                                        .type(type)
                                        .build()
                        )
                );
    }
}
