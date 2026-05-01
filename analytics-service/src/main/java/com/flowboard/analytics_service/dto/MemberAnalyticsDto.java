package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
This is for response dto for a member of a workspace so all the info will be according to
the workspace
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAnalyticsDto {
    private Integer userId;
    private String name;
    private String email;
    private String profilePicture;

    // cards still pending (To do + in progress)
    private long totalPending;

    private CardStatusSummaryDto cardsSummary;

}
