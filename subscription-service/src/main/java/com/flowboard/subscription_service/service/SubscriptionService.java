package com.flowboard.subscription_service.service;

import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponseDto buySubscription(SubscriptionRequestDto subscriptionRequestDto, Integer userId);

    SubscriptionResponseDto getDetails(Integer userId);

    List<SubscriptionPlanResponseDto> getPlanDetails();
}
