package com.learn.ms.getway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AuditFilter implements GlobalFilter, Ordered {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String cid = request.getHeaders().getFirst(LoggingFilter.CORRELATION_ID_HEADER);
        String user = request.getHeaders().getFirst("X-User-Id");
        if (user == null || user.isBlank()) {
            user = "anonymous";
        }
        String path = request.getURI().getRawPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        audit.info("AUDIT => user={} method={} path={} cid={} at={}", user, method, path, cid, Instant.now());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run after LoggingFilter
        return -90;
    }
}
