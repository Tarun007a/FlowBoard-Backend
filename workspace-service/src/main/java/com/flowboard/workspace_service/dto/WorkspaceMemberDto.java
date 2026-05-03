package com.flowboard.workspace_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberDto {
    private Integer memberId;

    private Integer workspaceId;

    private Integer userId;

    private LocalDateTime joinedAt;

}
