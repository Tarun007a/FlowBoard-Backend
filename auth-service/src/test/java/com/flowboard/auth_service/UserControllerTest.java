package com.flowboard.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.auth_service.controller.UserController;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserService userService;

    // ── GET /user-email/{email} ───────────────────────────────────────────

    @Test
    void getUserByEmail_found_returns200() throws Exception {
        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, true);
        when(userService.getUserByEmail("john@gmail.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/user-email/john@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    void getUserByEmail_notFound_returns400() throws Exception {
        when(userService.getUserByEmail("miss@x.com")).thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(get("/api/v1/user/user-email/miss@x.com")).andExpect(status().isBadRequest());
    }

    // ── GET /id/{userId} ─────────────────────────────────────────────────

    @Test
    void getUserById_found_returns200() throws Exception {
        UserDto dto = new UserDto("John", "j@j.com", "url", 1, true);
        when(userService.getUserById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getUserById_notFound_returns400() throws Exception {
        when(userService.getUserById(99)).thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(get("/api/v1/user/id/99")).andExpect(status().isBadRequest());
    }

    // ── DELETE /delete/{userId} ───────────────────────────────────────────

    @Test
    void deleteUser_sameUser_returns200() throws Exception {
        when(userService.deleteById(1, 1)).thenReturn("User deleted successfully");

        mockMvc.perform(delete("/api/v1/user/delete/1").header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void deleteUser_differentUser_returns400() throws Exception {
        when(userService.deleteById(99, 1)).thenThrow(new UserNotFoundException("mismatch"));

        mockMvc.perform(delete("/api/v1/user/delete/99").header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /update/{id} ─────────────────────────────────────────────────

    @Test
    void updateProfile_validPayload_returns202() throws Exception {
        UserUpdateDto req = new UserUpdateDto("Updated John", "newUrl");
        UserDto resp = new UserDto("Updated John", "j@j.com", "newUrl", 1, true);
        when(userService.updateProfile(any(), any(UserUpdateDto.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/user/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.fullName").value("Updated John"));
    }

    @Test
    void updateProfile_invalidPayload_returns400() throws Exception {
        UserUpdateDto req = new UserUpdateDto("", "url");
        mockMvc.perform(put("/api/v1/user/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /avtarurl/{id}/{url} ────────────────────────────────────────

    @Test
    void updateAvatarUrl_valid_returns202() throws Exception {
        UserDto dto = new UserDto("John", "j@j.com", "newUrl", 1, true);
        when(userService.updateAvatarUrl(1, "newUrl")).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/user/avtarurl/1/newUrl"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.avatarUrl").value("newUrl"));
    }

    @Test
    void updateAvatarUrl_notFound_returns400() throws Exception {
        when(userService.updateAvatarUrl(99, "url")).thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(patch("/api/v1/user/avtarurl/99/url")).andExpect(status().isBadRequest());
    }

    // ── GET /bulk ─────────────────────────────────────────────────────────

    @Test
    void getBulkUsers_valid_returns200() throws Exception {
        List<UserDto> users = List.of(
                new UserDto("A", "a@a.com", "url", 1, true),
                new UserDto("B", "b@b.com", "url", 2, true));
        when(userService.getBulkUser(any())).thenReturn(users);

        mockMvc.perform(get("/api/v1/user/bulk").param("userIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void getBulkUsers_serviceThrows_returns400() throws Exception {
        when(userService.getBulkUser(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/v1/user/bulk").param("userIds", "1")).andExpect(status().isBadRequest());
    }

    // ── GET /email/{id} ───────────────────────────────────────────────────

    @Test
    void getUserEmail_found_returns200() throws Exception {
        when(userService.getEmailById(1)).thenReturn("john@gmail.com");
        mockMvc.perform(get("/api/v1/user/email/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("john@gmail.com"));
    }

    @Test
    void getUserEmail_notFound_returns400() throws Exception {
        when(userService.getEmailById(99)).thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(get("/api/v1/user/email/99")).andExpect(status().isBadRequest());
    }

    // ── GET /findAll ──────────────────────────────────────────────────────

    @Test
    void getUserIdsByEmail_valid_returns200() throws Exception {
        when(userService.findAllUserIdByEmail(any())).thenReturn(List.of(1, 2));
        mockMvc.perform(get("/api/v1/user/findAll").param("userEmailList", "a@a.com", "b@b.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
    }

    @Test
    void getUserIdsByEmail_serviceThrows_returns400() throws Exception {
        when(userService.findAllUserIdByEmail(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/v1/user/findAll").param("userEmailList", "a@a.com"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /check/{userId} ───────────────────────────────────────────────

    @Test
    void checkUser_exists_returnsTrue() throws Exception {
        when(userService.checkByUserId(1)).thenReturn(true);
        mockMvc.perform(get("/api/v1/user/check/1"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    void checkUser_notExists_returnsFalse() throws Exception {
        when(userService.checkByUserId(99)).thenReturn(false);
        mockMvc.perform(get("/api/v1/user/check/99"))
                .andExpect(status().isOk()).andExpect(content().string("false"));
    }

    // ── GET /analytics/get/{userId} ───────────────────────────────────────

    @Test
    void analyticsGetUser_found_returns200() throws Exception {
        UserDto dto = new UserDto("John", "j@j.com", "url", 1, true);
        when(userService.getUserById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/analytics/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("j@j.com"));
    }

    @Test
    void analyticsGetUser_notFound_returns400() throws Exception {
        when(userService.getUserById(99)).thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(get("/api/v1/user/analytics/get/99")).andExpect(status().isBadRequest());
    }
}