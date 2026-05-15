package com.flowboard.auth_service;

import com.flowboard.auth_service.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailService, "apiKey", "test-key");
        ReflectionTestUtils.setField(emailService, "senderEmail", "no-reply@flowboard.com");
        ReflectionTestUtils.setField(emailService, "senderName", "FlowBoard");
        ReflectionTestUtils.setField(emailService, "adminVerifcationMail", "admin@flowboard.com");
    }

    // ── send ─────────────────────────────────────────────────────────────

    @Test
    void send_restTemplateThrows_doesNotPropagateException() {
        doThrow(new RuntimeException("boom")).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.send("to@x.com", "Subject", "<p>body</p>"));
    }

    // ── sendOtpEmail ──────────────────────────────────────────────────────

    @Test
    void sendOtpEmail_success_invokesRestTemplate() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        emailService.sendOtpEmail("to@x.com", "123456");
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void sendOtpEmail_restTemplateThrows_doesNotThrow() {
        doThrow(new RuntimeException()).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.sendOtpEmail("to@x.com", "123456"));
    }

    // ── sendVerificationEmail ─────────────────────────────────────────────

    @Test
    void sendVerificationEmail_success_invokesRestTemplate() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        emailService.sendVerificationEmail("to@x.com", "http://verify");
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void sendVerificationEmail_restTemplateThrows_doesNotThrow() {
        doThrow(new RuntimeException()).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.sendVerificationEmail("to@x.com", "http://verify"));
    }

    // ── sendVerificationEmailForAdmin ─────────────────────────────────────

    @Test
    void sendVerificationEmailForAdmin_success_invokesRestTemplate() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        emailService.sendVerificationEmailForAdmin("to@x.com", "http://verify");
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void sendVerificationEmailForAdmin_restTemplateThrows_doesNotThrow() {
        doThrow(new RuntimeException()).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.sendVerificationEmailForAdmin("to@x.com", "http://verify"));
    }

    // ── sendAccountActivationMail ─────────────────────────────────────────

    @Test
    void sendAccountActivationMail_success_invokesRestTemplate() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        emailService.sendAccountActivationMail("to@x.com");
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void sendAccountActivationMail_restTemplateThrows_doesNotThrow() {
        doThrow(new RuntimeException()).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.sendAccountActivationMail("to@x.com"));
    }

    // ── sendAccountDeactivatedMail ────────────────────────────────────────

    @Test
    void sendAccountDeactivatedMail_success_invokesRestTemplate() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        emailService.sendAccountDeactivatedMail("to@x.com");
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void sendAccountDeactivatedMail_restTemplateThrows_doesNotThrow() {
        doThrow(new RuntimeException()).when(restTemplate)
                .postForEntity(anyString(), any(), eq(String.class));
        assertDoesNotThrow(() -> emailService.sendAccountDeactivatedMail("to@x.com"));
    }
}