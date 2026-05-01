package com.flowboard.analytics_service.dto;

import lombok.*;

/*
this will be called for a workspace, a board or aa user so this will have all the cards related
info for that
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardStatusSummaryDto {
    private long toDo;
    private long overdueCards;
    private long inProgress;
    private long inReview;
    private long done;
    private long total;
    private long completionRate;
}
