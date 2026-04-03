package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signupRequestHandler(@Valid @RequestBody SignupDto signupDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(signupDto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginRequestHandler(@Valid @RequestBody LoginDto loginDto) {
        log.info("Login request");
        return ResponseEntity.ok().body(authService.login(loginDto));
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyUserAccount(@PathVariable String token) {
        authService.verify(token);
        return ResponseEntity.accepted().body("Account verified successfully, you can login now");
    }

    @PostMapping("/sendotp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        authService.sendOtp(email);
        return ResponseEntity.ok().body("OTP sent successfully");
    }

    @PostMapping("/forget")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody ForgetPasswordDto forgetPasswordDto) {
        authService.changePassword(forgetPasswordDto);
        return ResponseEntity.ok().body("Password changed successfully");
    }
}
