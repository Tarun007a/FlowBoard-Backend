package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BoardServiceClient {
    private final BaseWebClient webClient;

    public Mono<Integer> getTotalBoardsByWorkspace(Integer workspaceId) {
        return webClient.get("BOARD-SERVICE",
                "/api/v1/boards/analytics/user/" + workspaceId,
                Integer.class,
                "boardService");
    }
}
