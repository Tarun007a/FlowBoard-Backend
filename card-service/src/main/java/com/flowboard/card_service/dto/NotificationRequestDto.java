package com.flowboard.card_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotificationRequestDto {
    @Schema(description = "List of recipient user IDs", example = "[1,2,3]")
    private Integer recipientId;

    @Schema(description = "Actor (triggering user) ID", example = "10")
    private Integer actorId;

    @Schema(description = "Type of notification", example = "COMMENT")
    private NotificationType notificationType;

    @Schema(description = "Notification title", example = "Task Assigned")
    private String title;

    @Schema(description = "Notification message", example = "You have been assigned a new task")
    private String message;

    @Schema(description = "Related entity ID", example = "101")
    private Integer relatedId;

    @Schema(description = "Related entity type", example = "CARD")
    private RelatedType relatedType;
}
