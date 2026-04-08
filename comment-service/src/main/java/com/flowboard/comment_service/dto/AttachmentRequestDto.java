package com.flowboard.comment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttachmentRequestDto {
    @NotNull
    private Integer cardId;

    @NotNull
    private Integer uploaderId;
}