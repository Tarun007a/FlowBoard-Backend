package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.CardStatusSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CardServiceClient {
    private final BaseWebClient webClient;

    public Mono<CardStatusSummaryDto> getCardSummaryForWorkspace(Integer workspaceId) {
        return webClient.get("CARD-SERVICE",
                "/api/v1/cards/analytics/workspace/" + workspaceId,
                CardStatusSummaryDto.class,
                "cardService");
    }
}
