package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> handelFindByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(userService.getUserByEmail(email));
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserDto> handelFindById(@PathVariable Integer userId) {
        return ResponseEntity.ok().body(userService.getUserById(userId));
    }
}
