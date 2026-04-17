package com.flowboard.subscription_service.controller;

import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @PostMapping("/buy")
    public ResponseEntity<SubscriptionResponseDto> buySubscription(
            @Valid @RequestBody SubscriptionRequestDto subscriptionRequestDto,
            HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.buySubscription(subscriptionRequestDto, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<SubscriptionResponseDto> getSubscription(HttpServletRequest request) {
        Integer userId = extractUserId(request);

        return ResponseEntity.ok(subscriptionService.getDetails(userId));
    }

    @GetMapping("/details")
    public ResponseEntity<List<SubscriptionPlanResponseDto>> getSubscription() {
        return ResponseEntity.ok(subscriptionService.getPlanDetails());
    }
}
