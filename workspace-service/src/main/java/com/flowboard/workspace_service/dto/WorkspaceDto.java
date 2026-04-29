package com.flowboard.workspace_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {
    private Integer workspaceId;
    private String name;
    private String description;
    private Integer ownerId;
    private String visibility;
    private LocalDateTime createdAt;
}
