package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceClient {
    private final BaseWebClient webClient;

    public Mono<Boolean> isUserSubscribed(Integer userId) {
        return webClient.get(
                "SUBSCRIPTION-SERVICE",
                "/api/v1/subscriptions/check/" + userId,
                Boolean.class,
                "subscriptionService"
        );
    }
}
