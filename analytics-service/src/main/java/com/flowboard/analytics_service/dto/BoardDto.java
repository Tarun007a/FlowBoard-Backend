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
public class BoardDto {
    private Integer boardId;

    private Integer workspaceId;

    private String name;

    private String description;

    private String visibility;

    private Boolean isClosed;

    private LocalDateTime createdAt;

}
