package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Integer cardId;

    private Integer listId;

    private Integer boardId;

    private String title;

    private String description;

    private Integer position;

    private String priority;

    private String status;

    private LocalDateTime dueDate;

    private LocalDateTime startDate;

    private Integer assigneeId;

    private Integer createdById;

    private Boolean isArchived;

    private String coverColor;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
