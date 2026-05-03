package com.flowboard.card_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
