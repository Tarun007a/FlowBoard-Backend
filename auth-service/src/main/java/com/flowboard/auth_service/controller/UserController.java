package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.AppConstants;
import com.flowboard.auth_service.utils.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> handelDeleteById(@PathVariable Integer userId) {
        return ResponseEntity.ok().body(userService.deleteById(userId));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<CustomPageResponse<UserDto>> handleGetUserByRole(@PathVariable String role,
                                                                           @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                           @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                           @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                           @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.findAllByRole(role, page, size, sortBy, direction));
    }

    @GetMapping("/name/{fullName}")
    public ResponseEntity<CustomPageResponse<UserDto>> handlerGetUserByFullName(@PathVariable String fullName,
                                                                                @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                                @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.searchByFullName(fullName, page, size, sortBy, direction));
    }
}
