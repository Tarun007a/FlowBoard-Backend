package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.BoardAnalyticsDto;
import com.flowboard.analytics_service.dto.BoardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

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

    public Mono<BoardDto> getBoardById(Integer boardId) {
        return webClient.get("BOARD-SERVICE",
                "/api/v1/boards/analytics/get/" + boardId,
                BoardDto.class,
                "boardService");
    }

    public Mono<List<BoardDto>> getBoards(Integer workspaceId) {
        ParameterizedTypeReference<List<BoardDto>> type =
                new ParameterizedTypeReference<List<BoardDto>>() {};

        return webClient.get("BOARD-SERVICE",
                "/api/v1/boards/analytics/get-all/" + workspaceId,
                type,
                "boardService");
    }
}
