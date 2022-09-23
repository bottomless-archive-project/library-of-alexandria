package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessor {

    private final DocumentLocationVisitor documentLocationVisitor;

    public List<DocumentLocationResult> processLocations(final List<DocumentLocation> documentLocations) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Future<DocumentLocationResult>> results = documentLocations.stream()
                    .map(documentLocation -> scope.fork(() -> visitDocumentLocation(documentLocation)))
                    .toList();

            scope.join();
            scope.throwIfFailed();

            return results.stream()
                    .map(Future::resultNow)
                    .toList();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e); //TODO
        }
    }

    private DocumentLocationResult visitDocumentLocation(final DocumentLocation documentLocation) {
        final DocumentLocationResult result = documentLocationVisitor.visitDocumentLocation(documentLocation);

        log.info("Finished visiting url: {}.", documentLocation.getLocation());

        return result;
    }
}
