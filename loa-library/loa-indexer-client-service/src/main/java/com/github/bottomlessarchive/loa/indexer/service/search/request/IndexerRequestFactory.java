package com.github.bottomlessarchive.loa.indexer.service.search.request;

import com.github.bottomlessarchive.loa.indexer.service.search.request.mapping.MappingConfigurationFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchField;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Service;

/**
 * Create the requests to the search engine, to query data about the indexed documents.
 */
@Service
@RequiredArgsConstructor
public class IndexerRequestFactory {

    private static final String DOCUMENT_INDEX = "vault_documents";

    private final MappingConfigurationFactory mappingConfigurationFactory;
    private final QueryBuilderFactory queryBuilderFactory;

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
     * Create a new request that can be used to check if a document exists in the index or not.
     *
     * @param documentId the id of the document to check
     * @return the created request
     */
    public GetRequest newDocumentExistsRequest(final String documentId) {
        final GetRequest getRequest = new GetRequest(DOCUMENT_INDEX, documentId);

        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        return getRequest;
    }

    /**
     * Create a request that will create the index in the search engine for document indexing.
     *
     * @return the created request
     */
    public CreateIndexRequest newCreateIndexRequest() {
        final String mappingConfiguration = mappingConfigurationFactory.newDocumentMappingConfiguration();

        return new CreateIndexRequest(DOCUMENT_INDEX)
                .mapping(mappingConfiguration, XContentType.JSON)
                .settings(
                        Settings.builder()
                                .put("index.number_of_shards", 10)
                                .put("index.codec", "best_compression")
                                .put("index.refresh_interval", "60s")
                );
    }

    /**
     * Create a request that will check if an index exists in the search engine.
     *
     * @return the created request
     */
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
                .size(searchContext.getResultSize())
                .highlighter(newHighlightBuilder());
    }


    private HighlightBuilder newHighlightBuilder() {
        return new HighlightBuilder()
                .field(SearchField.CONTENT.getName(), 350, 3)
                .order("score");
    }
}
