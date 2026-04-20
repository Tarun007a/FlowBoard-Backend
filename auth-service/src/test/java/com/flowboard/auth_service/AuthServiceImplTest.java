package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.OtpException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock(name = "signupRequestMapper")
    private Mapper<SignupDto, User> signupRequestMapper;

    @Mock(name = "userResponseMapper")
    private Mapper<User, UserDto> userResponseMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserVerificationService userVerificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private UserOtpService userOtpService;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private UserVerificationRepository userVerificationRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_withValidData_returnsUserDto() {

        ReflectionTestUtils.setField(authService, "url", "http://localhost:8081/api/v1/");

        SignupDto signupDto =
                new SignupDto("John", "john@gmail.com", "Password@1");

        User user = new User();
        user.setEmail("john@gmail.com");
        user.setPassword("Password@1");

        User savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setEmail("john@gmail.com");

        UserVerification verification =
                UserVerification.builder()
                        .userId(1)
                        .token("token123")
                        .build();

        UserDto response =
                new UserDto("John", "john@gmail.com", null, 1);

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.empty());

        when(signupRequestMapper.mapTo(any()))
                .thenReturn(user);

        when(userResponseMapper.mapTo(any()))
                .thenReturn(response);

        when(passwordEncoder.encode("Password@1"))
                .thenReturn("encodedPassword");

        when(userVerificationService.save(any(UserVerification.class)))
                .thenReturn(verification);

        when(userResponseMapper.mapTo(savedUser))
                .thenReturn(response);

        UserDto result = authService.register(signupDto);

        assertEquals("john@gmail.com", result.getEmail());
        verify(emailService).sendVerificationEmail(any(), any());
    }

    @Test
    void register_withExistingActiveUser_throwsException() {

        SignupDto signupDto =
                new SignupDto("John", "john@gmail.com", "Password@1");

        User user = new User();
        user.setActive(true);

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class,
                () -> authService.register(signupDto));
    }

    @Test
    void login_withValidData_returnsToken() {

        LoginDto loginDto =
                new LoginDto("john@gmail.com", "Password@1");

        User user = new User();
        user.setUserId(1);

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken("john@gmail.com", "USER", 1))
                .thenReturn("jwt-token");

        String result = authService.login(loginDto);

        assertEquals("jwt-token", result);
    }

    @Test
    void login_withWrongEmail_throwsException() {

        LoginDto loginDto =
                new LoginDto("wrong@gmail.com", "Password@1");

        when(userRepository.findByEmail("wrong@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.login(loginDto));
    }

    @Test
    void verify_withValidToken_updatesUser() {

        UserVerification verification =
                UserVerification.builder()
                        .userId(1)
                        .token("abc")
                        .build();

        User user = new User();
        user.setUserId(1);
        user.setActive(false);

        when(userVerificationService.findByToken("abc"))
                .thenReturn(verification);

        when(userService.findById(1))
                .thenReturn(user);

        authService.verify("abc");

        verify(userRepository).save(user);
    }

    @Test
    void sendOtp_withValidEmail_callsService() {

        doNothing().when(userOtpService)
                .sendOtp("john@gmail.com");

        authService.sendOtp("john@gmail.com");

        verify(userOtpService).sendOtp("john@gmail.com");
    }

    @Test
    void changePassword_withValidOtp_updatesPassword() {

        ForgetPasswordDto dto =
                new ForgetPasswordDto(
                        "john@gmail.com",
                        "123456",
                        "NewPass@1"
                );

        User user = new User();
        user.setUserId(1);

        UserOtp otp = new UserOtp();
        otp.setUserId(1);
        otp.setOtp("123456");
        otp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.encode("NewPass@1"))
                .thenReturn("encoded");

        authService.changePassword(dto);

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withWrongOtp_throwsException() {

        ForgetPasswordDto dto =
                new ForgetPasswordDto(
                        "john@gmail.com",
                        "999999",
                        "NewPass@1"
                );

        User user = new User();
        user.setUserId(1);

        UserOtp otp = new UserOtp();
        otp.setUserId(1);
        otp.setOtp("123456");
        otp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(otp));

        assertThrows(OtpException.class,
                () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_withExpiredOtp_throwsException() {

        ForgetPasswordDto dto =
                new ForgetPasswordDto(
                        "john@gmail.com",
                        "123456",
                        "NewPass@1"
                );

        User user = new User();
        user.setUserId(1);

        UserOtp otp = new UserOtp();
        otp.setUserId(1);
        otp.setOtp("123456");
        otp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(otp));

        assertThrows(OtpException.class,
                () -> authService.changePassword(dto));
    }
}