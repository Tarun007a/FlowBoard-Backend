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
public class WorkspaceDto {
    private Integer workspaceId;
    private String name;
    private String description;
    private Integer ownerId;
    private String visibility;
    private LocalDateTime createdAt;
}
