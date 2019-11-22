package com.github.loa.parser.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.parser.domain.DocumentMetadata;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataParser {

    private final Parser documentParser;
    private final LanguageDetector languageDetector;
    private final VaultClientService vaultClientService;

    public Mono<DocumentMetadata> parseDocumentData(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> parseDocumentMetadata(documentEntity.getId(), documentEntity.getType(), documentContent))
                .onErrorContinue((throwable, document) -> log.debug("Failed to parse document!", throwable));
    }

    //TODO: Can we remove this somehow? Loading the full document contents to the memory is not always necessary.
    public DocumentMetadata parseDocumentMetadata(final String documentId, final DocumentType documentType,
            final byte[] documentContents) {
        return parseDocumentMetadata(documentId, documentType, new ByteArrayInputStream(documentContents));
    }

    public DocumentMetadata parseDocumentMetadata(final String documentId, final DocumentType documentType,
            final InputStream documentContents) {
        final ContentHandler handler = new BodyContentHandler(-1);
        final Metadata metadata = new Metadata();
        final ParseContext context = new ParseContext();

        try {
            documentParser.parse(TikaInputStream.get(documentContents), handler, metadata, context);
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }

        final int pageCount = Optional.ofNullable(metadata.getInt(PagedText.N_PAGES)).orElse(0);
        final Language language = languageDetector.detectLanguageOf(handler.toString());

        return DocumentMetadata.builder()
                .id(documentId)
                .title(metadata.get(TikaCoreProperties.TITLE))
                .author(metadata.get(TikaCoreProperties.CREATOR))
                .date(metadata.get(TikaCoreProperties.CREATED))
                .content(handler.toString())
                .language(language.getIsoCode())
                .type(documentType)
                .pageCount(pageCount)
                .build();
    }
}
