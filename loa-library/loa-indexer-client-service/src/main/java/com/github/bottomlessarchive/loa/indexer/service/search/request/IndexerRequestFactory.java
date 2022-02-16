package com.github.bottomlessarchive.loa.indexer.service.search.request;

import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.IndexOptions;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterOrder;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create the requests to the search engine, to query data about the indexed documents.
 */
@Service
@RequiredArgsConstructor
public class IndexerRequestFactory {

    private static final String DOCUMENT_INDEX = "vault_documents";

    private final QueryBuilderFactory queryBuilderFactory;

    /**
     * Create a request that will return the number of documents that are indexed successfully.
     *
     * @return the created request
     */
    public CountRequest newCountDocumentsRequest() {
        return new CountRequest.Builder()
                .index(DOCUMENT_INDEX)
                .query(QueryBuilders.matchAll().build()._toQuery())
                .build();
    }

    /**
     * Create a new request that can be used to check if a document exists in the index or not.
     *
     * @param documentId the id of the document to check
     * @return the created request
     */
    public co.elastic.clients.elasticsearch.core.ExistsRequest newDocumentExistsRequest(final String documentId) {
        return new co.elastic.clients.elasticsearch.core.ExistsRequest.Builder()
                .index(DOCUMENT_INDEX)
                .id(documentId)
                .storedFields(List.of("_none_"))
                .build();
    }

    /**
     * Create a request that will create the index in the search engine for document indexing.
     *
     * @return the created request
     */
    public CreateIndexRequest newCreateIndexRequest() {
        return new CreateIndexRequest.Builder()
                .index(DOCUMENT_INDEX)
                .mappings(new TypeMapping.Builder()
                        .properties(
                                Map.of(
                                        "author", new Property.Builder()
                                                .text(
                                                        new TextProperty.Builder()
                                                                .build()
                                                )
                                                .build(),
                                        "content", new Property.Builder()
                                                .text(
                                                        new TextProperty.Builder()
                                                                .indexOptions(IndexOptions.Offsets)
                                                                .build()
                                                )
                                                .build(),
                                        "title", new Property.Builder()
                                                .text(
                                                        new TextProperty.Builder()
                                                                .build()
                                                )
                                                .build(),
                                        "date", new Property.Builder()
                                                .date(
                                                        new DateProperty.Builder()
                                                                .build()
                                                )
                                                .build(),
                                        "language", new Property.Builder()
                                                .keyword(
                                                        new KeywordProperty.Builder()
                                                                .build()
                                                )
                                                .build(),
                                        "type", new Property.Builder()
                                                .keyword(
                                                        new KeywordProperty.Builder()
                                                                .build()
                                                )
                                                .build(),
                                        "page_count", new Property.Builder()
                                                .long_(
                                                        new LongNumberProperty.Builder()
                                                                .build()
                                                )
                                                .build()
                                )
                        )
                        .build()
                )
                .settings(
                        new IndexSettings.Builder()
                                .numberOfShards("10")
                                .codec("best_compression")
                                .refreshInterval(
                                        new Time.Builder()
                                                .time("60s")
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    /**
     * Create a request that will check if an index exists in the search engine.
     *
     * @return the created request
     */
    public ExistsRequest newIndexExistsRequest() {
        return new ExistsRequest.Builder()
                .index(DOCUMENT_INDEX)
                .build();
    }

    /**
     * Create a search request based on a provided {@link SearchContext}. The searched word will be highlighted.
     *
     * @param searchContext the context of the search
     * @return the created request
     */
    public SearchRequest newKeywordSearchRequest(final SearchContext searchContext) {
        return new SearchRequest.Builder()
                .index(DOCUMENT_INDEX)
                .from(searchContext.getPageNumber())
                .size(searchContext.getResultSize())
                .highlight(newHighlightBuilder())
                .query(queryBuilderFactory.newQueryBuilder(searchContext))
                .build();
    }

    private Highlight newHighlightBuilder() {
        return new Highlight.Builder()
                .fields(SearchField.CONTENT.getName(), new HighlightField.Builder()
                        .field(SearchField.CONTENT.getName())
                        .fragmentSize(350)
                        .numberOfFragments(3)
                        .build()
                )
                .order(HighlighterOrder.Score)
                .build();
    }
}
