package com.flowboard.analytics_service.controller;

import com.flowboard.analytics_service.dto.*;
import com.flowboard.analytics_service.exception.IllegalOperationException;
import com.flowboard.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Controller", description = "Analytics APIs for user and workspace insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    private Integer getUserId(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getHeader("X-User-Id"));
        }
        catch (Exception e) {
            throw new IllegalOperationException("User not logged In, header not found");
        }
    }
    @Operation(
            summary = "Get user analytics",
            description = "Returns a workspace overview card for every workspace the user belongs to. " +
                    "Each card includes board count, member count, and full card status summary with overdue. " +
                    "Requires active subscription."
    )
    @ApiResponse(responseCode = "200", description = "User analytics fetched successfully")
    @GetMapping("/me")
    public ResponseEntity<Mono<List<WorkspaceOverviewDto>>> getUserAnalytics(HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getUserAnalytics(userId));
    }

    @Operation(
            summary = "Get workspace analytics",
            description = "Returns full analytics for a workspace: counts, card summary, lightweight member list, " +
                    "and lightweight board list. Member and board detail are loaded lazily on click. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Workspace analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<WorkspaceAnalyticsResponseDto> getWorkspaceAnalytics(
            @PathVariable Integer workspaceId,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getWorkspaceAnalytics(workspaceId, userId));
    }

    @Operation(
            summary = "Get member analytics",
            description = "Returns full analytics for a single member inside a workspace: " +
                    "name, email, avatar, total pending, missed deadlines, and full card status summary. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Member analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}/member/{memberId}")
    public ResponseEntity<MemberAnalyticsDto> getMemberAnalytics(
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getMemberAnalytics(workspaceId, memberId, userId));
    }


    @Operation(
            summary = "Get board analytics",
            description = "Returns full analytics for a single board inside a workspace: " +
                    "list count, card count, card status summary, and all lists for the filter panel. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Board analytics fetched successfully")
    @GetMapping("/workspace/{workspaceId}/board/{boardId}")
    public ResponseEntity<BoardAnalyticsDto> getBoardAnalytics(
            @PathVariable Integer workspaceId,
            @PathVariable Integer boardId,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getBoardAnalytics(workspaceId, boardId, userId));
    }


    @Operation(
            summary = "Get filtered workspace cards",
            description = "Returns cards across all boards in a workspace filtered by status, due date, and assignee. " +
                    "status: TO_DO | IN_PROGRESS | IN_REVIEW | DONE. " +
                    "due: TODAY | THIS_WEEK | OVERDUE. " +
                    "assigneeId: optional — if omitted workspace owners see all, members see their own. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Filtered workspace cards fetched successfully")
    @GetMapping("/workspace/{workspaceId}/cards")
    public ResponseEntity<List<CardDto>> getWorkspaceCards(
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String due,
            @RequestParam(required = false) Integer assigneeId,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getWorkspaceCards(workspaceId, userId, status, due, assigneeId));
    }

    @Operation(
            summary = "Get filtered board cards",
            description = "Returns cards in a single board filtered by status, due date, and assignee. " +
                    "Same filter params as workspace cards. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Filtered board cards fetched successfully")
    @GetMapping("/workspace/{workspaceId}/board/{boardId}/cards")
    public ResponseEntity<List<CardDto>> getBoardCards(
            @PathVariable Integer workspaceId,
            @PathVariable Integer boardId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String due,
            @RequestParam(required = false) Integer assigneeId,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getBoardCards(workspaceId, boardId, userId, status, due, assigneeId));
    }

    @Operation(
            summary = "Get filtered member cards",
            description = "Returns cards assigned to a specific member inside a workspace, " +
                    "filtered by status and due date. " +
                    "Requires active subscription and workspace membership."
    )
    @ApiResponse(responseCode = "200", description = "Filtered member cards fetched successfully")
    @GetMapping("/workspace/{workspaceId}/member/{memberId}/cards")
    public ResponseEntity<List<CardDto>> getMemberCards(
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String due,
            HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(analyticsService.getMemberCards(workspaceId, memberId, userId, status, due));
    }
}
