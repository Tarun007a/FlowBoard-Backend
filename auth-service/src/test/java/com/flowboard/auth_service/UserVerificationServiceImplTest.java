package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.TokenNotFoundException;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.impl.UserVerificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceImplTest {

    @Mock private UserVerificationRepository userVerificationRepository;

    @InjectMocks
    private UserVerificationServiceImpl userVerificationService;

    private UserVerification uv(int userId, String token) {
        return UserVerification.builder().userId(userId).token(token).build();
    }

    @Test
    void save_returnsSavedEntity() {
        UserVerification v = uv(1, "tok");
        when(userVerificationRepository.save(v)).thenReturn(v);
        assertEquals("tok", userVerificationService.save(v).getToken());
    }

    @Test
    void deleteByUserId_found_deletesRecord() {
        UserVerification v = uv(1, "tok");
        when(userVerificationRepository.findByUserId(1)).thenReturn(Optional.of(v));

        userVerificationService.deleteByUserId(1);
        verify(userVerificationRepository).delete(v);
    }

    @Test
    void deleteByUserId_notFound_throwsUsernameNotFoundException() {
        when(userVerificationRepository.findByUserId(99)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userVerificationService.deleteByUserId(99));
    }

    @Test
    void findByToken_found_returnsEntity() {
        UserVerification v = uv(1, "tok123");
        when(userVerificationRepository.findByToken("tok123")).thenReturn(Optional.of(v));
        assertEquals("tok123", userVerificationService.findByToken("tok123").getToken());
    }

    @Test
    void findByToken_notFound_throwsTokenNotFoundException() {
        when(userVerificationRepository.findByToken("bad")).thenReturn(Optional.empty());
        assertThrows(TokenNotFoundException.class, () -> userVerificationService.findByToken("bad"));
    }
}