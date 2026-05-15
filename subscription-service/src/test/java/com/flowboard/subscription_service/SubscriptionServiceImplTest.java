package com.flowboard.subscription_service;

import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.entity.UserSubscription;
import com.flowboard.subscription_service.exception.UserNotFoundException;
import com.flowboard.subscription_service.mapper.Mapper;
import com.flowboard.subscription_service.repository.SubscriptionRepository;
import com.flowboard.subscription_service.service.RazorPayService;
import com.flowboard.subscription_service.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private Mapper<UserSubscription, SubscriptionResponseDto> responseMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RazorPayService razorPayService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                subscriptionService,
                "keySecret",
                "secret"
        );
    }

    private String generateSignature(String payload)
            throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(
                new SecretKeySpec(
                        "secret".getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );

        byte[] hash =
                mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }

    @Test
    void buySubscription_positive() {

        SubscriptionRequestDto dto =
                new SubscriptionRequestDto();

        when(razorPayService.getSubscription(dto))
                .thenReturn(new RazorPayResponseDto());

        assertNotNull(
                subscriptionService.buySubscription(dto, 1)
        );
    }

    @Test
    void verifyAndActivate_positive() throws Exception {

        PaymentVerificationDto dto =
                new PaymentVerificationDto();

        dto.setRazorpayOrderId("order");
        dto.setRazorpayPaymentId("payment");
        dto.setPlan(SubscriptionPlan.BASIC);

        String payload = "order|payment";

        dto.setRazorpaySignature(
                generateSignature(payload)
        );

        UserSubscription saved =
                UserSubscription.builder()
                        .userId(1)
                        .plan(SubscriptionPlan.BASIC)
                        .startDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusDays(30))
                        .status("Active")
                        .build();

        when(subscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(saved);

        when(responseMapper.mapTo(saved))
                .thenReturn(new SubscriptionResponseDto());

        SubscriptionResponseDto result =
                subscriptionService.verifyAndActivate(dto, 1);

        assertNotNull(result);
    }

    @Test
    void verifyAndActivate_invalidSignature_throwsException() {

        PaymentVerificationDto dto =
                new PaymentVerificationDto();

        dto.setRazorpayOrderId("order");
        dto.setRazorpayPaymentId("payment");
        dto.setRazorpaySignature("wrong");
        dto.setPlan(SubscriptionPlan.BASIC);

        assertThrows(RuntimeException.class,
                () -> subscriptionService.verifyAndActivate(dto, 1));
    }

    @Test
    void getDetails_positive() {

        UserSubscription subscription =
                UserSubscription.builder()
                        .userId(1)
                        .build();

        when(subscriptionRepository.findByUserId(1))
                .thenReturn(Optional.of(subscription));

        when(responseMapper.mapTo(subscription))
                .thenReturn(new SubscriptionResponseDto());

        assertNotNull(
                subscriptionService.getDetails(1)
        );
    }

    @Test
    void getDetails_negative() {

        when(subscriptionRepository.findByUserId(99))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> subscriptionService.getDetails(99));
    }

    @Test
    void getPlanDetails_positive() {

        List<SubscriptionPlanResponseDto> result =
                subscriptionService.getPlanDetails();

        assertEquals(3, result.size());
    }

    @Test
    void isSubscribed_true() {

        when(subscriptionRepository.existsByUserId(1))
                .thenReturn(true);

        assertTrue(
                subscriptionService.isSubscribed(1)
        );
    }

    @Test
    void isSubscribed_false() {

        when(subscriptionRepository.existsByUserId(1))
                .thenReturn(false);

        assertFalse(
                subscriptionService.isSubscribed(1)
        );
    }
}