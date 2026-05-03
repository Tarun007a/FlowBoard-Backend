package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/*
Breaker name is the name/id of circuit breaker instance in you .yml,
 */
@Service
@RequiredArgsConstructor
public class AuthServiceClient {
    private final BaseWebClient webClient;
    public Mono<UserDto> getUserById(Integer memberId) {
        return webClient.get("AUTH-SERVICE",
                "/api/v1/user/analytics/get/" + memberId,
                 UserDto.class,
                "authService");
    }
}