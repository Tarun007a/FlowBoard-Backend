package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceOverviewDto {
    private Integer workspaceId;

    private String name;

    private String logoUrl;

    private boolean isOwner;

    private int totalBoards;

    private int totalMembers;

    private CardStatusSummaryDto cardsSummary;
}
