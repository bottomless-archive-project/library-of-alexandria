package com.github.loa.indexer.service;

import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.domain.SearchDocumentEntity;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final SearchRequestFactory searchRequestFactory;
    private final SearchDocumentEntityTransformer searchDocumentEntityTransformer;

    public List<SearchDocumentEntity> searchDocuments(final String keyword) {
        final SearchRequest searchRequest = searchRequestFactory.newKeywordSearchRequest(keyword);

        try {
            final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            return searchDocumentEntityTransformer.transform(searchResponse.getHits());
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }
}
