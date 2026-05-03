package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.ListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListServiceClient {
    private final BaseWebClient webClient;

    public Mono<Integer> getTotalListByWorkspace(Integer workspaceId) {
        return webClient.get("LIST-SERVICE",
                "/api/v1/lists/analytics/workspace/total/" + workspaceId,
                Integer.class,
                "listService");
    }

    public Mono<Integer> getTotalListsByBoard(Integer boardId) {
        return webClient.get("LIST-SERVICE",
                "/api/v1/lists/analytics/count/" + boardId,
                Integer.class,
                "listService");
    }

    public Mono<List<ListDto>> getLists(Integer boardId) {
        ParameterizedTypeReference<List<ListDto>> type =
                new ParameterizedTypeReference<List<ListDto>>() {};

        return webClient.get("LIST-SERVICE",
                "/api/v1/lists/analytics/get-all/" + boardId,
                type,
                "listService");
    }
}
