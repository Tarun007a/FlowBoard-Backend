package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.impl.UserServiceImpl;
import com.flowboard.auth_service.utils.CustomPageResponse;
import com.flowboard.auth_service.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private Mapper<User, UserDto> userResponseMapper;
    @Mock private SecurityUtils securityUtils;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    // helpers
    private User user(int id, String email) {
        User u = new User(); u.setUserId(id); u.setEmail(email); return u;
    }
    private UserDto dto(int id, String email) {
        UserDto d = new UserDto(); d.setUserId(id); d.setEmail(email); return d;
    }

    // ── getUserByEmail ────────────────────────────────────────────────────

    @Test
    void getUserByEmail_found_returnsDto() {
        User u = user(1, "john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(u));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "john@gmail.com"));

        assertEquals("john@gmail.com", userService.getUserByEmail("john@gmail.com").getEmail());
    }

    @Test
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("x@x.com"));
    }

    // ── getUserById ───────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsDto() {
        User u = user(1, "j@j.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        assertEquals(1, userService.getUserById(1).getUserId());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99));
    }

    // ── updateProfile ─────────────────────────────────────────────────────

    @Test
    void updateProfile_valid_updatesAndReturns() {
        User u = user(1, "j@j.com");
        UserUpdateDto upd = new UserUpdateDto("New Name", "newUrl");
        User saved = user(1, "j@j.com"); saved.setFullName("New Name"); saved.setAvatarUrl("newUrl");
        UserDto resp = new UserDto(); resp.setFullName("New Name"); resp.setAvatarUrl("newUrl");

        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        when(userRepository.save(u)).thenReturn(saved);
        when(userResponseMapper.mapTo(saved)).thenReturn(resp);

        UserDto result = userService.updateProfile(1, upd);
        assertEquals("New Name", result.getFullName());
        assertEquals("newUrl", result.getAvatarUrl());
    }

    @Test
    void updateProfile_notFound_throwsException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> userService.updateProfile(1, new UserUpdateDto("X", "url")));
    }

    // ── deleteById ────────────────────────────────────────────────────────

    @Test
    void deleteById_sameUser_deletesAndReturnsMessage() {
        User u = user(1, "j@j.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        String msg = userService.deleteById(1, 1);
        assertEquals("User deleted successfully", msg);
        verify(userRepository).delete(u);
    }

    @Test
    void deleteById_differentUser_throwsException() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteById(1, 2));
    }

    // ── deactivateAccount ─────────────────────────────────────────────────

    @Test
    void deactivateAccount_valid_setsInactive() {
        User u = user(1, "j@j.com"); u.setActive(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        userService.deactivateAccount(1);
        assertFalse(u.isActive());
    }

    @Test
    void deactivateAccount_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deactivateAccount(99));
    }

    // ── findAllByRole ─────────────────────────────────────────────────────

    @Test
    void findAllByRole_ascDirection_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.findAllByRole(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        CustomPageResponse<UserDto> result = userService.findAllByRole("MEMBER", 0, 10, "userId", "asc");
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllByRole_descDirection_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.findAllByRole(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        CustomPageResponse<UserDto> result = userService.findAllByRole("MEMBER", 0, 10, "userId", "desc");
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAllByRole_invalidRole_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findAllByRole("INVALID", 0, 10, "userId", "asc"));
    }

    // ── searchByFullName ──────────────────────────────────────────────────

    @Test
    void searchByFullName_asc_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.searchByFullName(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        assertEquals(1, userService.searchByFullName("John", 0, 10, "userId", "asc").getContent().size());
    }

    @Test
    void searchByFullName_desc_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.searchByFullName(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        assertEquals(1, userService.searchByFullName("John", 0, 10, "userId", "desc").getContent().size());
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void findAll_asc_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        assertEquals(1, userService.findAll(0, 10, "userId", "asc").getContent().size());
    }

    @Test
    void findAll_desc_returnsPage() {
        User u = user(1, "j@j.com");
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u)));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        assertEquals(1, userService.findAll(0, 10, "userId", "desc").getContent().size());
    }

    // ── findById ──────────────────────────────────────────────────────────

    @Test
    void findById_found_returnsUser() {
        User u = user(1, "j@j.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        assertEquals(1, userService.findById(1).getUserId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(99));
    }

    // ── updateAvatarUrl ───────────────────────────────────────────────────

    @Test
    void updateAvatarUrl_sameUser_updatesAndReturns() {
        User logged = user(1, "j@j.com");
        User target = user(1, "j@j.com");
        UserDto resp = new UserDto(); resp.setAvatarUrl("newUrl");

        when(securityUtils.getLoggedInUserEmail()).thenReturn("j@j.com");
        when(userRepository.findByEmail("j@j.com")).thenReturn(Optional.of(logged));
        when(userRepository.findById(1)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);
        when(userResponseMapper.mapTo(target)).thenReturn(resp);

        assertEquals("newUrl", userService.updateAvatarUrl(1, "newUrl").getAvatarUrl());
    }

    @Test
    void updateAvatarUrl_differentUser_throwsIllegalArgument() {
        User logged = user(1, "j@j.com");
        User target = user(2, "other@x.com");

        when(securityUtils.getLoggedInUserEmail()).thenReturn("j@j.com");
        when(userRepository.findByEmail("j@j.com")).thenReturn(Optional.of(logged));
        when(userRepository.findById(2)).thenReturn(Optional.of(target));

        assertThrows(IllegalArgumentException.class, () -> userService.updateAvatarUrl(2, "url"));
    }

    @Test
    void updateAvatarUrl_loggedUserNotFound_throwsException() {
        when(securityUtils.getLoggedInUserEmail()).thenReturn("miss@x.com");
        when(userRepository.findByEmail("miss@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateAvatarUrl(1, "url"));
    }

    // ── deleteUser ────────────────────────────────────────────────────────

    @Test
    void deleteUser_callsDeleteById() {
        userService.deleteUser(5);
        verify(userRepository).deleteById(5);
    }

    // ── disable / enable ──────────────────────────────────────────────────

    @Test
    void disable_setsInactiveAndSendsDeactivatedMail() {
        User u = user(1, "j@j.com"); u.setActive(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        userService.disable(1);

        assertFalse(u.isActive());
        verify(emailService).sendAccountDeactivatedMail("j@j.com");
        verify(userRepository).save(u);
    }

    @Test
    void disable_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.disable(99));
    }

    @Test
    void enable_setsActiveAndSendsActivationMail() {
        User u = user(1, "j@j.com"); u.setActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        userService.enable(1);

        assertTrue(u.isActive());
        verify(emailService).sendAccountActivationMail("j@j.com");
        verify(userRepository).save(u);
    }

    @Test
    void enable_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.enable(99));
    }

    // ── searchByEmail ─────────────────────────────────────────────────────

    @Test
    void searchByEmail_found_returnsDto() {
        User u = user(1, "j@j.com");
        when(userRepository.findByEmail("j@j.com")).thenReturn(Optional.of(u));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));
        assertEquals("j@j.com", userService.searchByEmail("j@j.com").getEmail());
    }

    @Test
    void searchByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("miss@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.searchByEmail("miss@x.com"));
    }

    // ── getEmailById ──────────────────────────────────────────────────────

    @Test
    void getEmailById_found_returnsEmail() {
        User u = user(1, "j@j.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        assertEquals("j@j.com", userService.getEmailById(1));
    }

    @Test
    void getEmailById_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getEmailById(99));
    }

    // ── findAllUserIdByEmail ──────────────────────────────────────────────

    @Test
    void findAllUserIdByEmail_allFound_returnsIds() {
        User u1 = user(1, "a@a.com"); User u2 = user(2, "b@b.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u1));
        when(userRepository.findByEmail("b@b.com")).thenReturn(Optional.of(u2));

        List<Integer> ids = userService.findAllUserIdByEmail(List.of("a@a.com", "b@b.com"));
        assertEquals(2, ids.size());
    }

    @Test
    void findAllUserIdByEmail_someNotFound_skipsMissing() {
        User u1 = user(1, "a@a.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u1));
        when(userRepository.findByEmail("miss@x.com")).thenReturn(Optional.empty());

        List<Integer> ids = userService.findAllUserIdByEmail(List.of("a@a.com", "miss@x.com"));
        assertEquals(1, ids.size());
        assertEquals(1, ids.get(0));
    }

    // ── getBulkUser ───────────────────────────────────────────────────────

    @Test
    void getBulkUser_withIds_returnsMappedDtos() {
        User u = user(1, "j@j.com");
        when(userRepository.findAllByUserIdIn(List.of(1))).thenReturn(List.of(u));
        when(userResponseMapper.mapTo(u)).thenReturn(dto(1, "j@j.com"));

        List<UserDto> result = userService.getBulkUser(List.of(1));
        assertEquals(1, result.size());
    }

    @Test
    void getBulkUser_emptyList_returnsEmpty() {
        when(userRepository.findAllByUserIdIn(any())).thenReturn(List.of());
        assertEquals(0, userService.getBulkUser(List.of()).size());
    }

    // ── checkByUserId ─────────────────────────────────────────────────────

    @Test
    void checkByUserId_found_returnsTrue() {
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
        assertTrue(userService.checkByUserId(1));
    }

    @Test
    void checkByUserId_notFound_returnsFalse() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertFalse(userService.checkByUserId(99));
    }
}