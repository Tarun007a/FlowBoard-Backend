
package com.flowboard.analytics_service.client;

import com.flowboard.analytics_service.config.BaseWebClient;
import com.flowboard.analytics_service.dto.WorkspaceDto;
import com.flowboard.analytics_service.dto.WorkspaceMemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceClient {

    private final BaseWebClient webClient;

    public Mono<List<WorkspaceDto>> getWorkspacesByUser(Integer userId) {
        ParameterizedTypeReference<List<WorkspaceDto>> type =
                new ParameterizedTypeReference<>() {};

        return webClient.get(
                "WORKSPACE-SERVICE",
                "/api/v1/workspaces/analytics/user/" + userId,
                type,
                "workspaceService"
        );
    }

    public Mono<Integer> getTotalMemberInWorkspace(Integer workspaceId) {
        return webClient.get("WORKSPACE-SERVICE",
                "/api/v1/workspaces/analytics/members/" + workspaceId,
                Integer.class,
                "workspaceService"
        );
    }

    public Mono<Boolean> isOwner(Integer workspaceId, Integer userId) {
        return webClient.get("WORKSPACE-SERVICE",
                "/api/v1/workspaces/analytics/check-owner/" + workspaceId + "/" + userId,
                Boolean.class,
                "workspaceService");
    }

    public Mono<WorkspaceDto> getWorkspaceById(Integer workspaceId) {
        return webClient.get("WORKSPACE-SERVICE",
                "/api/v1/workspaces/analytics/get/" + workspaceId,
                WorkspaceDto.class,
                "workspaceService");
    }

    public Mono<List<WorkspaceMemberDto>> getMembers(Integer workspaceId) {
        ParameterizedTypeReference<List<WorkspaceMemberDto>> type =
                new ParameterizedTypeReference<List<WorkspaceMemberDto>>() {};

        return webClient.get("WORKSPACE-SERVICE",
                "/api/v1/workspaces/analytics/get-all/members/" + workspaceId,
                type,
                "workspaceService");
    }
}