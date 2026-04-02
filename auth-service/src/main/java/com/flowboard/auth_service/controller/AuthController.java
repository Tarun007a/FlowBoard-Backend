package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Boolean> loginRequestHandler(@Valid @RequestBody LoginDto loginDto) {
        log.info("Login request");
        return ResponseEntity.ok().body(authService.login(loginDto));
    }
}
