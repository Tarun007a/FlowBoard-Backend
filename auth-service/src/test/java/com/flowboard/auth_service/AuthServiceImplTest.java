package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.impl.SignupRequestMapper;
import com.flowboard.auth_service.Mapper.impl.UserResponseMapper;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.TokenNotFoundException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.JwtService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.service.UserVerificationService;
import com.flowboard.auth_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SignupRequestMapper signupRequestMapper;
    @Mock
    private UserResponseMapper userResponseMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserVerificationService userVerificationService;
    @Mock private EmailService emailService;
    @Mock private UserService userService;
    @Mock private UserOtpService userOtpService;
    @Mock private UserOtpRepository userOtpRepository;
    @Mock private UserVerificationRepository userVerificationRepository;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    void register_withNewEmail_savesUserAndSendsVerificationEmail() {
        ReflectionTestUtils.setField(authService, "url", "http://localhost/");

        SignupDto dto = new SignupDto("Tarun", "tarun@gmail.com", "Pass@1");
        User mapped = new User(); mapped.setEmail("tarun@gmail.com"); mapped.setPassword("Pass@1");
        User saved  = new User(); saved.setUserId(1); saved.setEmail("tarun@gmail.com");
        UserVerification uv = UserVerification.builder().userId(1).token("tok").build();
        UserDto response = new UserDto(); response.setUserId(1); response.setEmail("tarun@gmail.com");

        when(userRepository.findByEmail("tarun@gmail.com")).thenReturn(Optional.empty());
        when(signupRequestMapper.mapTo(dto)).thenReturn(mapped);
        when(passwordEncoder.encode("Pass@1")).thenReturn("enc");
        when(userRepository.save(any())).thenReturn(saved);
        when(userVerificationService.save(any())).thenReturn(uv);
        when(userResponseMapper.mapTo(saved)).thenReturn(response);

        UserDto result = authService.register(dto);

        assertEquals(1, result.getUserId());
        verify(emailService).sendVerificationEmail(eq("tarun@gmail.com"), contains("tok"));
    }

    @Test
    void register_withExistingActiveUser_throwsUserNotFoundException() {
        User active = new User(); active.setActive(true);
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.of(active));

        assertThrows(UserNotFoundException.class,
                () -> authService.register(new SignupDto("X", "x@x.com", "P@ss1234")));
    }

    @Test
    void register_withExistingInactiveUser_deletesOldRecordAndRegisters() {
        ReflectionTestUtils.setField(authService, "url", "http://localhost/");

        User old = new User(); old.setUserId(9); old.setActive(false);
        User mapped = new User(); mapped.setPassword("p");
        User saved  = new User(); saved.setUserId(1);
        UserVerification uv = UserVerification.builder().userId(1).token("t").build();
        UserDto response = new UserDto(); response.setUserId(1);

        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(old));
        when(signupRequestMapper.mapTo(any())).thenReturn(mapped);
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(userRepository.save(any())).thenReturn(saved);
        when(userVerificationService.save(any())).thenReturn(uv);
        when(userResponseMapper.mapTo(saved)).thenReturn(response);

        authService.register(new SignupDto("A", "a@a.com", "P@ss1234"));

        verify(userRepository).delete(old);
        verify(userVerificationRepository).deleteByUserId(9);
    }

    // ── registerAdmin ─────────────────────────────────────────────────────

//    @Test
//    void registerAdmin_withNewEmail_setsAdminRoleAndSendsEmail() {
//        ReflectionTestUtils.setField(authService, "url", "http://localhost");
//
//        SignupDto dto = new SignupDto("Admin", "admin@gmail.com", "P@ss1234");
//        User mapped = new User();
//        mapped.setPassword("p");
//        User saved  = new User();
//        saved.setUserId(2);
//        saved.setEmail("admin@gmail.com");
//        UserVerification uv = UserVerification.builder().userId(2).token("adminTok").build();
//        UserDto response = new UserDto(); response.setUserId(2);
//
//        when(userRepository.findByEmail("admin@gmail.com"))
//                .thenReturn(Optional.empty());
//        when(signupRequestMapper.mapTo(dto)).thenReturn(mapped);
//        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
//        when(userRepository.save(any())).thenReturn(saved);
//        when(userVerificationService.save(any())).thenReturn(uv);
//        when(userResponseMapper.mapTo(saved)).thenReturn(response);
//
//        UserDto result = authService.registerAdmin(dto);
//
//        assertEquals(2, result.getUserId());
//        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
//        verify(userRepository).save(cap.capture());
//        assertEquals(ROLE.PLATFORM_ADMIN, cap.getValue().getRole());
//        verify(emailService).sendVerificationEmailForAdmin(eq("admin@gmail.com"), contains("adminTok"));
//    }

    @Test
    void registerAdmin_withExistingUser_throwsException() {
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserNotFoundException.class,
                () -> authService.registerAdmin(new SignupDto("A", "admin@gmail.com", "P@ss1234")));
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returnsJwtToken() {
        LoginDto dto = new LoginDto("john@gmail.com", "P@ss1234");
        User user = new User(); user.setUserId(1); user.setEmail("john@gmail.com"); user.setRole(ROLE.MEMBER);

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("john@gmail.com", "MEMBER", 1)).thenReturn("jwt-token");

        assertEquals("jwt-token", authService.login(dto));
    }

    @Test
    void login_withUnknownEmail_throwsUserNotFoundException() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.login(new LoginDto("x@x.com", "P@ss1234")));
    }

    @Test
    void login_withBadCredentials_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginDto("john@gmail.com", "wrong")));
    }

    // ── verify ────────────────────────────────────────────────────────────

    @Test
    void verify_withValidToken_activatesUser() {
        UserVerification uv = UserVerification.builder().userId(1).token("abc").build();
        User user = new User(); user.setUserId(1); user.setActive(false);

        when(userVerificationService.findByToken("abc")).thenReturn(uv);
        when(userService.findById(1)).thenReturn(user);

        authService.verify("abc");

        assertTrue(user.isActive());
        verify(userVerificationService).deleteByUserId(1);
        verify(userRepository).save(user);
    }

    @Test
    void verify_withInvalidToken_throwsTokenNotFoundException() {
        when(userVerificationService.findByToken("bad"))
                .thenThrow(new TokenNotFoundException("not found"));

        assertThrows(TokenNotFoundException.class, () -> authService.verify("bad"));
    }

    // ── sendOtp ───────────────────────────────────────────────────────────

    @Test
    void sendOtp_delegatesToUserOtpService() {
        authService.sendOtp("john@gmail.com");
        verify(userOtpService).sendOtp("john@gmail.com");
    }

    @Test
    void sendOtp_propagatesOtpException() {
        doThrow(new OtpException("limit")).when(userOtpService).sendOtp("john@gmail.com");
        assertThrows(OtpException.class, () -> authService.sendOtp("john@gmail.com"));
    }

    // ── changePassword ────────────────────────────────────────────────────

    @Test
    void changePassword_withValidOtp_updatesPassword() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "abc123", "New@1234");
        User user = new User(); user.setUserId(1);
        UserOtp otp = new UserOtp(); otp.setUserId(1); otp.setOtp("abc123");
        otp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("New@1234")).thenReturn("enc");

        authService.changePassword(dto);

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withExpiredOtp_throwsOtpException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "abc123", "New@1234");
        User user = new User(); user.setUserId(1);
        UserOtp otp = new UserOtp(); otp.setUserId(1); otp.setOtp("abc123");
        otp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withWrongOtp_throwsOtpException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "wrong", "New@1234");
        User user = new User(); user.setUserId(1);
        UserOtp otp = new UserOtp(); otp.setUserId(1); otp.setOtp("correct");
        otp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withMismatchedOtpOwner_throwsOtpException() {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@gmail.com", "abc123", "New@1234");
        User user = new User(); user.setUserId(1);
        UserOtp otp = new UserOtp(); otp.setUserId(2); otp.setOtp("abc123");
        otp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(1)).thenReturn(Optional.of(otp));

        assertThrows(OtpException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withUnknownEmail_throwsUserNotFoundException() {
        when(userRepository.findByEmail("miss@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> authService.changePassword(new ForgetPasswordDto("miss@x.com", "1", "P")));
    }

    @Test
    void changePassword_withNoOtpRecord_throwsOtpException() {
        User user = new User(); user.setUserId(5);
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userOtpRepository.findByUserId(5)).thenReturn(Optional.empty());

        assertThrows(OtpException.class,
                () -> authService.changePassword(new ForgetPasswordDto("john@gmail.com", "1", "P")));
    }
}