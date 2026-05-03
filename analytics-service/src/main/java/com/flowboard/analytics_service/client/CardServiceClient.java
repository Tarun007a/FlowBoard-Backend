package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.CardDto;
import com.flowboard.analytics_service.dto.CardStatusSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CardServiceClient {
    private final BaseWebClient webClient;

    public Mono<CardStatusSummaryDto> getCardSummaryForWorkspace(Integer workspaceId) {
        return webClient.get("CARD-SERVICE",
                "/api/v1/cards/analytics/summary/workspace/" + workspaceId,
                CardStatusSummaryDto.class,
                "cardService");
    }

    public Mono<CardStatusSummaryDto> getCardSummaryForUser(Integer workspaceId, Integer userId) {
        return webClient.get("CARD-SERVICE",
                "/api/v1/cards/analytics/user/" + workspaceId + "/" + userId,
                CardStatusSummaryDto.class,
                "cardService");
    }

    public Mono<CardStatusSummaryDto> getCardSummaryForBoard(Integer boardId) {
        return webClient.get("CARD-SERVICE",
                "/api/v1/cards/analytics/board/" + boardId,
                CardStatusSummaryDto.class,
                "cardService");
    }

    public Mono<List<CardDto>> getCardsByWorkspace(Integer workspaceId) {
        ParameterizedTypeReference<List<CardDto>> type =
                new ParameterizedTypeReference<List<CardDto>>() {};

        return webClient.get("CARD-SERVICE",
                "/api/v1/cards/analytics/workspace/" + workspaceId,
                type,
                "cardService");
    }
}
