package com.flowboard.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgetPasswordDto {
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 6)
    private String otp;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain 1 lowercase, 1 uppercase, 1 digit, size 8")
    private String newPassword;
}
