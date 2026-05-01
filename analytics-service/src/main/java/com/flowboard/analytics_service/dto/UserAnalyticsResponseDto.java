package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponseDto {

    // -- Overview --
    private Integer userId;
    private String userName;
    private String userEmail;
    private String profilePicture;

    // workspaces the user owns
    private int totalWorkspacesOwned;

    // workspaces the user is a member of (including owned)
    private int totalWorkspacesMemberOf;

    // total boards across all workspaces the user is a member of
    private int totalBoards;

    // -- Cards assigned to this user across ALL workspaces --
    private CardStatusSummaryDto assignedCardsSummary;

    // overdue cards assigned to user
    private long overdueCards;

    // -- Cards created by this user across ALL workspaces --
    private long totalCardsCreated;

    // -- Per-workspace summary list shown on dashboard --
    private List<WorkspaceOverviewDto> workspaces;
}
