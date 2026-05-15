package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.impl.UserOtpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOtpServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserOtpRepository userOtpRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserOtpServiceImpl userOtpService;

    private User user(int id, String email) {
        User u = new User(); u.setUserId(id); u.setEmail(email); return u;
    }

    // ── first OTP request ─────────────────────────────────────────────────

    @Test
    void sendOtp_noExistingOtp_savesNewRecordAndSendsEmail() {
        User u = user(1, "john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(u));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.empty());

        userOtpService.sendOtp("john@gmail.com");

        verify(userOtpRepository).save(any(UserOtp.class));
        verify(emailService).sendOtpEmail(eq("john@gmail.com"), any());
    }

    // ── re-send after 5 minutes ───────────────────────────────────────────

    @Test
    void sendOtp_existingOtpAfter5Min_updatesOtpAndResends() {
        User u = user(1, "john@gmail.com");
        UserOtp existing = new UserOtp();
        existing.setUserId(1); existing.setOtpSent(1);
        existing.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(u));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        userOtpService.sendOtp("john@gmail.com");

        verify(userOtpRepository).save(existing);
        verify(emailService).sendOtpEmail(any(), any());
    }

    // ── throttle: within 5 minutes ────────────────────────────────────────

    @Test
    void sendOtp_existingOtpBefore5Min_throwsOtpException() {
        User u = user(1, "john@gmail.com");
        UserOtp existing = new UserOtp();
        existing.setUserId(1); existing.setOtpSent(1);
        existing.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(u));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        assertThrows(OtpException.class, () -> userOtpService.sendOtp("john@gmail.com"));
    }

    // ── daily limit reached ────────────────────────────────────────────────

    @Test
    void sendOtp_dailyLimitReached_throwsOtpException() {
        User u = user(1, "john@gmail.com");
        UserOtp existing = new UserOtp();
        existing.setUserId(1); existing.setOtpSent(5);
        existing.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(u));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        assertThrows(OtpException.class, () -> userOtpService.sendOtp("john@gmail.com"));
    }

    // ── unknown email ─────────────────────────────────────────────────────

    @Test
    void sendOtp_unknownEmail_throwsUserNotFoundException() {
        when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userOtpService.sendOtp("wrong@gmail.com"));
    }
}