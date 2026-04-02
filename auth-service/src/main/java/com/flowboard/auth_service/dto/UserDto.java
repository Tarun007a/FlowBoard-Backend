package com.flowboard.auth_service.dto;

import lombok.Data;

@Data
public class UserDto {
    private String fullName;

    private String email;

    private String avatarUrl;

    private Integer userId;
}
