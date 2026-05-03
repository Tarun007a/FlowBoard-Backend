package com.flowboard.board_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
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
