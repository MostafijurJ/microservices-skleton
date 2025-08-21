package com.learn.ms.getway.filter;

import lombok.extern.slf4j.Slf4j;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();

        String requestPath = exchange.getRequest().getURI().getPath();
        exchange.getRequest().getMethod();
        String method = exchange.getRequest().getMethod().name();

        log.info("➡ Request started: {} {} at {}", method, requestPath, startTime);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Instant endTime = Instant.now();
            long durationMillis = Duration.between(startTime, endTime).toMillis();

            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            log.info("✅ Response completed: {} {} | Status: {} | Duration: {} ms", method, requestPath, statusCode, durationMillis);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
