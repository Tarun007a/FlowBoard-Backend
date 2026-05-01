package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceAnalyticsResponseDto {

    // Workspace info
    private Integer workspaceId;
    private String name;
    private String description;
    private String logoUrl;
    private String visibility;
    private LocalDateTime createdAt;

    // counts
    private int totalBoards;
    private int totalLists;
    private int totalMembers;

    // Card status breakdown across all boards in this workspace
    private CardStatusSummaryDto cardsSummary;
}
