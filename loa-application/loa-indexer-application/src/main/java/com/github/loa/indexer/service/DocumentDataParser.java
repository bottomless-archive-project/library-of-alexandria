package com.github.loa.indexer.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.service.index.domain.DocumentMetadata;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataParser {

    private final VaultClientService vaultClientService;
    //TODO: This should come from a config!
    private final LanguageDetector languageDetector = LanguageDetectorBuilder.fromAllBuiltInLanguages().build();

    public Mono<DocumentMetadata> parseDocumentData(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> parseDocumentMetadata(documentEntity, documentContent))
                .onErrorContinue((throwable, document) -> log.debug("Failed to parse document!", throwable));
    }

    private DocumentMetadata parseDocumentMetadata(final DocumentEntity documentEntity, final byte[] documentContents) {
        final Parser parser = new AutoDetectParser();
        final ContentHandler handler = new BodyContentHandler(-1);
        final Metadata metadata = new Metadata();
        final ParseContext context = new ParseContext();

        try {
            parser.parse(new ByteArrayInputStream(documentContents), handler, metadata, context);
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }

        int pageCount = 0;
        if (documentEntity.isPdf()) {
            try (final PDDocument pdDocument = PDDocument.load(documentContents)) {
                pageCount = pdDocument.getNumberOfPages();
            } catch (IOException e) {
                log.info("Removing document {} because of a parse failure.", documentEntity.getId(), e);

                vaultClientService.removeDocument(documentEntity).subscribe();

                throw new RuntimeException(e);
            }
        }

        final Language language = languageDetector.detectLanguageOf(handler.toString());

        return DocumentMetadata.builder()
                .id(documentEntity.getId())
                .title(metadata.get(TikaCoreProperties.TITLE))
                .author(metadata.get(TikaCoreProperties.CREATOR))
                .date(metadata.get(TikaCoreProperties.CREATED))
                .content(handler.toString())
                .language(language.getIsoCode())
                .type(documentEntity.getType())
                .pageCount(pageCount)
                .build();
    }
}
