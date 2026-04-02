package com.flowboard.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String fullName;

    private String avatarUrl;
}
