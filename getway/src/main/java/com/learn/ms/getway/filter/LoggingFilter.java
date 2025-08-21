package com.learn.ms.getway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        // Ensure correlation id exists
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        ServerWebExchange effectiveExchange = exchange;
        if (correlationId == null || correlationId.isBlank()) {
            final String newCid = UUID.randomUUID().toString();
            effectiveExchange = exchange.mutate()
                    .request(builder -> builder.header(CORRELATION_ID_HEADER, newCid))
                    .build();
            correlationId = newCid;
        }
        final String cid = correlationId;

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String remote = remoteAddress != null ? remoteAddress.toString() : "unknown";

        // Log request basics
        log.info("GW IN => method={} path={} cid={} remote={} at={}",
                request.getMethod(), request.getURI().getRawPath(), cid, remote, Instant.now());

        // Optionally log selected headers (avoid sensitive data)
        List<String> userAgent = request.getHeaders().getOrEmpty("User-Agent");
        if (!userAgent.isEmpty()) {
            log.debug("GW IN H => User-Agent={} cid={}", String.join(",", userAgent), cid);
        }

        final ServerWebExchange exForChain = effectiveExchange;
        return chain.filter(exForChain)
                .doOnSuccess(v -> {
                    ServerHttpResponse response = exForChain.getResponse();
                    long took = System.currentTimeMillis() - start;
                    log.info("GW OUT => status={} cid={} durationMs={}", response.getStatusCode(), cid, took);
                })
                .doOnError(ex -> {
                    long took = System.currentTimeMillis() - start;
                    log.error("GW ERR => cid={} durationMs={} error={}", cid, took, ex.toString());
                });
    }

    @Override
    public int getOrder() {
        // Run early to set correlation id
        return -100;
    }
}
