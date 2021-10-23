package com.github.bottomlessarchive.loa.web.view.document.request.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IndexPageWebFilter implements WebFilter {

    private static final String INDEX_PATH_WITHOUT_FILENAME = "/";

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        if (INDEX_PATH_WITHOUT_FILENAME.equals(exchange.getRequest().getURI().getPath())) {
            return chain.filter(
                    exchange.mutate()
                            .request(
                                    exchange.getRequest()
                                            .mutate()
                                            .path("/index.html")
                                            .build()
                            )
                            .build()
            );
        }

        return chain.filter(exchange);
    }
}
