package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardRequestDto {
    @NotNull(message = "List id cannot be null")
    private Integer listId;

    @NotNull(message = "Board id cannot be null")
    private Integer boardId;

    @NotNull(message = "Title cannot be null")
    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private LocalDateTime dueDate;

    private LocalDateTime startDate;

    /* Validate that this assignee have card modification request */
    private Integer assigneeId;
}