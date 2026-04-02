package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final Mapper<SignupDto, User> signupRequestMapper;
    private final Mapper<User, UserDto> userResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDto register(SignupDto signupDto) {
        User user = signupRequestMapper.mapTo(signupDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return userResponseMapper.mapTo(savedUser);
    }

    @Override
    public boolean login(LoginDto loginDto) {
        log.info("login service");
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken
                        (loginDto.getEmail(), loginDto.getPassword()));
        log.info("login service");
        return authentication.isAuthenticated();
    }

    @Override
    public void changePassword(Integer id, String Password) {

    }
}
