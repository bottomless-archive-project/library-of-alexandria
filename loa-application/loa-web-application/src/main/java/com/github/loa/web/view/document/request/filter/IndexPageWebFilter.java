package com.github.loa.web.view.document.request.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IndexPageWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        if (exchange.getRequest().getURI().getPath().equals("/")) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest()
                            .mutate()
                            .path("/index.html")
                            .build()
                    )
                    .build());
        }

        return chain.filter(exchange);
    }
}
