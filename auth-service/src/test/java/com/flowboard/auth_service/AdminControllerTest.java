package com.flowboard.auth_service;

import com.flowboard.auth_service.controller.AdminController;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AdminController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;

    private CustomPageResponse<UserDto> pageOf(UserDto dto) {
        return new CustomPageResponse<>(new PageImpl<>(List.of(dto)));
    }

    // ── GET /name/{fullName} ──────────────────────────────────────────────

    @Test
    void searchByFullName_returns200WithContent() throws Exception {
        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, true);
        when(userService.searchByFullName(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageOf(dto));

        mockMvc.perform(get("/api/v1/admin/name/John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@gmail.com"));
    }

    @Test
    void searchByFullName_serviceThrows_returns400() throws Exception {
        when(userService.searchByFullName(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/v1/admin/name/John")).andExpect(status().isBadRequest());
    }

    // ── GET /email/{email} ────────────────────────────────────────────────

    @Test
    void searchByEmail_returns200WithUser() throws Exception {
        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, true);
        when(userService.searchByEmail("john@gmail.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/email/john@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    void searchByEmail_notFound_returns400() throws Exception {
        when(userService.searchByEmail("miss@x.com"))
                .thenThrow(new UserNotFoundException("not found"));
        mockMvc.perform(get("/api/v1/admin/email/miss@x.com")).andExpect(status().isBadRequest());
    }

    // ── GET /user/all ─────────────────────────────────────────────────────

    @Test
    void getAllUsers_returns200WithPage() throws Exception {
        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, true);
        when(userService.findAll(anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageOf(dto));

        mockMvc.perform(get("/api/v1/admin/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(1));
    }

    @Test
    void getAllUsers_serviceThrows_returns400() throws Exception {
        when(userService.findAll(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/v1/admin/user/all")).andExpect(status().isBadRequest());
    }

    // ── DELETE /{userId} ──────────────────────────────────────────────────

    @Test
    void deleteUser_returns200() throws Exception {
        doNothing().when(userService).deleteUser(1);
        mockMvc.perform(delete("/api/v1/admin/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void deleteUser_serviceThrows_returns400() throws Exception {
        doThrow(new RuntimeException("error")).when(userService).deleteUser(1);
        mockMvc.perform(delete("/api/v1/admin/1")).andExpect(status().isBadRequest());
    }

    // ── PUT /disable/{userId} ─────────────────────────────────────────────

    @Test
    void disableUser_returns200() throws Exception {
        doNothing().when(userService).disable(2);
        mockMvc.perform(put("/api/v1/admin/disable/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("User disabled successfully"));
    }

    @Test
    void disableUser_serviceThrows_returns400() throws Exception {
        doThrow(new RuntimeException("error")).when(userService).disable(2);
        mockMvc.perform(put("/api/v1/admin/disable/2")).andExpect(status().isBadRequest());
    }

    // ── PUT /enable/{userId} ──────────────────────────────────────────────

    @Test
    void enableUser_returns200() throws Exception {
        doNothing().when(userService).enable(3);
        mockMvc.perform(put("/api/v1/admin/enable/3"))
                .andExpect(status().isOk())
                .andExpect(content().string("User enabled successfully"));
    }

    @Test
    void enableUser_serviceThrows_returns400() throws Exception {
        doThrow(new RuntimeException("error")).when(userService).enable(3);
        mockMvc.perform(put("/api/v1/admin/enable/3")).andExpect(status().isBadRequest());
    }
}