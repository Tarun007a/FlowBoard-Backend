package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardUpdateDto {
    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private LocalDateTime dueDate;

    private LocalDateTime startDate;
}
