package com.flowboard.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.auth_service.controller.AuthController;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.service.AuthService;
import com.flowboard.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;

    // ── POST /signup ──────────────────────────────────────────────────────

    @Test
    void signup_validPayload_returns201WithUserDto() throws Exception {
        SignupDto dto = new SignupDto("John Doe", "john@example.com", "Password@1");
        UserDto resp = new UserDto("John Doe", "john@example.com", null, 1, false);
        when(authService.register(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void signup_invalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupDto("", "", ""))))
                .andExpect(status().isBadRequest());
    }

    // ── POST /login ───────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        LoginDto dto = new LoginDto("john@example.com", "Password@1");
        when(authService.login(any())).thenReturn("jwt-token-xyz");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token-xyz"));
    }

    @Test
    void login_badCredentials_returns400() throws Exception {
        LoginDto dto = new LoginDto("john@example.com", "Password@1");
        when(authService.login(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /verify/{token} ───────────────────────────────────────────────

    @Test
    void verify_validToken_returns202() throws Exception {
        doNothing().when(authService).verify("valid-tok");

        mockMvc.perform(get("/api/v1/auth/verify/valid-tok"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Account verified successfully, you can login now"));
    }

    @Test
    void verify_invalidToken_returns400() throws Exception {
        doThrow(new RuntimeException("not found")).when(authService).verify("bad-tok");

        mockMvc.perform(get("/api/v1/auth/verify/bad-tok"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /sendotp ─────────────────────────────────────────────────────

    @Test
    void sendOtp_registered_returns200() throws Exception {
        doNothing().when(authService).sendOtp("john@example.com");

        mockMvc.perform(post("/api/v1/auth/sendotp").param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully"));
    }

    @Test
    void sendOtp_unregistered_returns400() throws Exception {
        doThrow(new UserNotFoundException("not found")).when(authService).sendOtp("ghost@x.com");

        mockMvc.perform(post("/api/v1/auth/sendotp").param("email", "ghost@x.com"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /forget ──────────────────────────────────────────────────────

    @Test
    void forgetPassword_validOtp_returns200() throws Exception {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@example.com", "abc123", "NewPass@123");
        doNothing().when(authService).changePassword(any());

        mockMvc.perform(post("/api/v1/auth/forget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    @Test
    void forgetPassword_wrongOtp_returns400() throws Exception {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@example.com", "000000", "NewPass@123");
        doThrow(new OtpException("invalid otp")).when(authService).changePassword(any());

        mockMvc.perform(post("/api/v1/auth/forget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /register-admin ──────────────────────────────────────────────

    @Test
    void registerAdmin_validPayload_returns201() throws Exception {
        SignupDto dto = new SignupDto("Admin User", "admin@example.com", "Password@1");
        UserDto resp = new UserDto("Admin User", "admin@example.com", null, 10, false);
        when(authService.registerAdmin(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void registerAdmin_invalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupDto("", "", ""))))
                .andExpect(status().isBadRequest());
    }

    // ── GET /is-admin ─────────────────────────────────────────────────────

    @Test
    void isAdmin_platformAdmin_returnsTrue() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is-admin").header("X-User-Role", "PLATFORM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isAdmin_memberRole_returnsFalse() throws Exception {
        mockMvc.perform(get("/api/v1/auth/is-admin").header("X-User-Role", "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}