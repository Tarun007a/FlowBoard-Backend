package com.flowboard.analytics_service.config;

import com.flowboard.analytics_service.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/*
Here we are using circuit  breaker + webclient
if you not want circuit breaker simply
 WebClient client = builder.baseUrl("http://" + serviceName).build();
 return client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(responseType)
            .timeout(Duration.ofSeconds(10));

private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
here one ? is configType and other is builderType currently we dont care about type so we used
wildcards to keep it simple
 */
@Component
@RequiredArgsConstructor
public class BaseWebClient {

    private final WebClient.Builder builder;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public <T> Mono<T> get(String serviceName,
                           String uri,
                           Class<T> responseType,
                           String breakerName) {

        WebClient client = builder.baseUrl("http://" + serviceName).build();

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create(breakerName);

        return circuitBreaker.run(
                () -> client.get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(responseType)
                        .timeout(Duration.ofSeconds(10)),

                throwable -> Mono.error(
                        new ServiceUnavailableException(
                                serviceName + " is unreachable"
                        )
                )
        );
    }

    // This is to return For List / generic types
    public <T> Mono<T> get(String serviceName,
                           String uri,
                           ParameterizedTypeReference<T> responseType,
                           String breakerName) {

        WebClient client = builder.baseUrl("http://" + serviceName).build();
        CircuitBreaker cb = circuitBreakerFactory.create(breakerName);

        return cb.run(
                () -> client.get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(responseType)
                        .timeout(Duration.ofSeconds(10)),

                throwable -> Mono.error(
                        new ServiceUnavailableException(
                                serviceName + " is unreachable"
                        )
                )
        );
    }
}