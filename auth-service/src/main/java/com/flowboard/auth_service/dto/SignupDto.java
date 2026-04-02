package com.flowboard.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupDto {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 to 50 character")
    private String fullName;

    @Email(message = "Enter a valid email address")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain 1 lowercase, 1 uppercase, 1 digit, size 8")
    private String password;
}
