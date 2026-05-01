package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardAnalyticsDto {
    private Integer boardId;

    private String name;

    private String visibility;

    private Boolean isClosed;

    private int totalLists;

    private CardStatusSummaryDto cardsSummary;

}
