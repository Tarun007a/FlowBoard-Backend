package com.flowboard.auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    public String generateToken(String username, String role, Integer userId);
}
