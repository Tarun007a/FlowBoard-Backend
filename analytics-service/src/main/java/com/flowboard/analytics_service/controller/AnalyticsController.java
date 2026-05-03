package com.flowboard.analytics_service.controller;

import com.flowboard.analytics_service.dto.*;
import com.flowboard.analytics_service.enums.CardStatus;
import com.flowboard.analytics_service.enums.DueFilter;
import com.flowboard.analytics_service.service.AnalyticsService;
import com.flowboard.analytics_service.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/*
The problem with ResponseEntity<Mono<List<...>>> is that you're giving Spring a
ResponseEntity whose body is an unsubscribed pipeline. Spring will just try
to serialize the Mono object itself, not its emitted value so you may get error at runtime.
Stay reactive end-to-end (proper WebFlux) - Return Mono<List<WorkspaceOverviewDto>>
as every thing is only returning to a request and we don't need any specific response code -200 works well.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Controller", description = "Analytics APIs for user and workspace insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Get user analytics",
            description = "Returns a workspace overview card for every workspace the user belongs to. " +
                    "Each card includes board count, member count, and full card status summary with overdue. " +
                    "Requires active subscription."
    )
    @ApiResponse(responseCode = "200", description = "User analytics fetched successfully")
    @GetMapping("/me")
    public Mono<List<WorkspaceOverviewDto>> getUserAnalytics(@RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getUserAnalytics(userId);
    }

    @Operation(
            summary = "Get workspace analytics",
            description = "Returns full analytics for a workspace: counts, card summary, lightweight member list, " +
                    "and lightweight board list. Member and board detail are loaded lazily on click. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Workspace analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}")
    public Mono<WorkspaceAnalyticsResponseDto> getWorkspaceAnalytics(
            @PathVariable Integer workspaceId,
            @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getWorkspaceAnalytics(workspaceId, userId);
    }

    @Operation(
            summary = "Get member analytics",
            description = "Returns full analytics for a single member inside a workspace: " +
                    "name, email, avatar, total pending, missed deadlines, and full card status summary. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Member analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}/member/{memberId}")
    public Mono<MemberAnalyticsDto> getMemberAnalytics(@PathVariable Integer workspaceId, @PathVariable Integer memberId, @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getMemberAnalytics(workspaceId, memberId, userId);
    }


    @Operation(
            summary = "Get board analytics",
            description = "Returns full analytics for a single board inside a workspace: " +
                    "list count, card count, card status summary, and all lists for the filter panel. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Board analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}/board/{boardId}")
    public Mono<BoardAnalyticsDto> getBoardAnalytics(@PathVariable Integer workspaceId,
                                                     @PathVariable Integer boardId,
                                                     @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getBoardAnalytics(workspaceId, boardId, userId);
    }

    @GetMapping("/cards")
    public Mono<List<CardDto>> getCards(@RequestParam Integer workspaceId,
                                        @RequestParam(required = false) Integer boardId,
                                        @RequestParam(required = false) CardStatus status,
                                        @RequestParam(required = false) DueFilter due,
                                        @RequestParam(required = false) Integer assigneeId,
                                        @RequestHeader("X-User-Id") Integer userId) {

        return analyticsService.getCards(workspaceId, boardId, userId, status, due, assigneeId);
    }

    @Operation(
            summary = "Get Workspace Member info",
            description = "Returns members inside the workspace " +
                    "showed just below the workspace Analytics when user get a " +
                    " specific workspace analytics"
    )
    @GetMapping("/workspace/members/{workspaceId}")
    public Mono<List<WorkspaceMemberDto>> getWorkspaceMembers(@PathVariable Integer workspaceId,
                                                        @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getWorkspaceMembers(workspaceId, userId);
    }

    @Operation(
            summary = "Get Workspace boards info",
            description = "Returns boards inside the workspace " +
                    "showed just below the workspace Analytics when user get a " +
                    " specific workspace analytics"
    )
    @GetMapping("/workspace/boards/{workspaceId}")
    public Mono<List<BoardDto>> getWorkspaceBoards(@PathVariable Integer workspaceId,
                                             @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getWorkspaceBoards(workspaceId, userId);
    }

    @Operation(
            summary = "All lists in a board",
            description = "Returns a list of all the List in a board"
    )
    @GetMapping("/board/list/{workspaceId}/{boardId}")
    public Mono<List<ListDto>> getBoardLists(@PathVariable Integer workspaceId,
                                             @PathVariable Integer boardId,
                                             @RequestHeader("X-User-Id") Integer userId) {
        return analyticsService.getBoardLists(boardId, workspaceId, userId);
    }
}
