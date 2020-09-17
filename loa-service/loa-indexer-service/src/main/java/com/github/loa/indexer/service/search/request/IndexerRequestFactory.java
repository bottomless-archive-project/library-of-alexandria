package com.github.loa.indexer.service.search.request;

import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.indexer.service.search.domain.SearchField;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Create the requests to the search engine, to query data about the indexed documents.
 */
@Service
@RequiredArgsConstructor
public class IndexerRequestFactory {

    private final ResourceLoader resourceLoader;
    private final QueryBuilderFactory queryBuilderFactory;

    private static final String DOCUMENT_INDEX = "vault_documents";

    /**
     * Create a request that will return the number of documents that are indexed successfully.
     *
     * @return the created request
     */
    public CountRequest newCountDocumentsRequest() {
        return new CountRequest(DOCUMENT_INDEX)
                .query(QueryBuilders.matchAllQuery());
    }

    /**
     * Create a request that will create the index in the search engine for document indexing.
     *
     * @return the created request
     */
    public CreateIndexRequest newCreateIndexRequest() {
        final String mappingConfiguration = loadMappingConfiguration();

        return new CreateIndexRequest(DOCUMENT_INDEX)
                .mapping(mappingConfiguration, XContentType.JSON)
                .settings(
                        Settings.builder()
                                .put("index.number_of_shards", 10)
                                .put("index.codec", "best_compression")
                                .put("index.refresh_interval", "60s")
                );
    }

    public GetIndexRequest newIndexExistsRequest() {
        return new GetIndexRequest(DOCUMENT_INDEX);
    }

    /**
     * Create a search request based on a provided {@link SearchContext}. The searched word will be highlighted.
     *
     * @param searchContext the context of the search
     * @return the created request
     */
    public SearchRequest newKeywordSearchRequest(final SearchContext searchContext) {
        return new SearchRequest(new String[]{DOCUMENT_INDEX}, newDocumentContentQuery(searchContext));
    }

    private SearchSourceBuilder newDocumentContentQuery(final SearchContext searchContext) {
        final QueryBuilder queryBuilder = queryBuilderFactory.newQueryBuilder(searchContext);

        return new SearchSourceBuilder()
                .from(searchContext.getPageNumber())
                .query(queryBuilder)
                .highlighter(newHighlightBuilder());
    }


    private HighlightBuilder newHighlightBuilder() {
        return new HighlightBuilder()
                .field(SearchField.CONTENT.getName(), 500, 1)
                .order("score");
    }

    //TODO: Move this to its own service!
    private String loadMappingConfiguration() {
        try {
            return new String(Files.readAllBytes(
                    resourceLoader.getResource("classpath:indexer/mapping.json").getFile().toPath()));
        } catch (IOException e) {
            //TODO: Use named exception!
            throw new RuntimeException(e);
        }
    }
}
