package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        User user = new User();
        user.setEmail("john@gmail.com");
        user.setPassword("pass");

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("john@gmail.com");
        assertEquals("john@gmail.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("miss@x.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("miss@x.com"));
    }
}