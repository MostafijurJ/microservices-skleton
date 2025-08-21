package com.learn.ms.getway.filter;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(value = Integer.MIN_VALUE)
public class AuditFilter implements WebFilter {

    @Value("${ignore.header-param}")
    private String[] ignored;

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        final long startTime = System.currentTimeMillis();
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        final var requestLog = new HashMap<String, Object>();
        requestLog.put("PATH", exchange.getRequest().getPath().value());
        requestLog.put("METHOD", exchange.getRequest().getMethod().name());
        requestLog.put("HEADER", getItem(exchange.getRequest().getHeaders()));
        requestLog.put("PARAM", getItem(exchange.getRequest().getQueryParams()));
        if (exchange.getRequest().getRemoteAddress() != null) {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress == null) {
                requestLog.put("REMOTE-ADDRESS", "unknown");
            } else {
                String hostString = remoteAddress.getHostString();
                requestLog.put("REMOTE-ADDRESS", hostString != null ? hostString : "unknown");
            }
        }
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    requestLog.put("DURATION", getDuration(startTime));
                    requestLog.put("STATUS", "SUCCESS");
                    if (!requestLog.get("PATH").toString().matches("^\\/actuator\\/[\\/\\w]*$")) {
                        log.info("REQUEST: {}", requestLog);
                    }
                })
                .doOnError(throwable -> {
                    requestLog.put("DURATION", getDuration(startTime));
                    requestLog.put("STATUS", "ERROR");
                    requestLog.put("STATUS-MESSAGE", throwable.getMessage());
                    log.info("REQUEST: {}", requestLog);
                });
    }

    private Map<String, String> getItem(MultiValueMap<String, String> multiValueMap) {
        Map<String, String> map = new HashMap<>();
        for (var entry : multiValueMap.entrySet()) {
            try {
                String key = entry.getKey();
                String value = entry.getValue().isEmpty() ? "" : entry.getValue().get(0);
                if (ignored != null && Arrays.asList(ignored).contains(key)) {
                    map.put(key, "********");
                } else {
                    map.put(key, value);
                }
            } catch (Exception e) {
                log.warn("Error processing header/query param: {}", entry.getKey(), e);
            }
        }
        return map;
    }

    private long getDuration(long startTime) {
        return Duration.ofMillis(System.currentTimeMillis() - startTime).toMillis();
    }
}
