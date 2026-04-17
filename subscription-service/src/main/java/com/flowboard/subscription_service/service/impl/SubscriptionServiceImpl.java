package com.flowboard.subscription_service.service.impl;

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
import com.flowboard.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final Mapper<UserSubscription, SubscriptionResponseDto> responseMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final RazorPayService razorPayService;
    @Override
    public SubscriptionResponseDto buySubscription(SubscriptionRequestDto subscriptionRequestDto, Integer userId) {
        RazorPayResponseDto razorPayResponseDto = razorPayService.getSubscription(subscriptionRequestDto);
        SubscriptionPlan subscription = subscriptionRequestDto.getPlan();
        LocalDate now = LocalDate.now();

        UserSubscription userSubscription = UserSubscription
                .builder()
                .userId(userId)
                .expiryDate(now.plusDays(subscription.getDurationDays()))
                .status("Active")
                .startDate(now)
                .plan(subscription)
                .build();

        UserSubscription savedUserSubscription = subscriptionRepository.save(userSubscription);

        return responseMapper.mapTo(savedUserSubscription);
    }

    @Override
    public SubscriptionResponseDto getDetails(Integer userId) {
        UserSubscription userSubscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return responseMapper.mapTo(userSubscription);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getPlanDetails() {
        SubscriptionPlanResponseDto pro = SubscriptionPlanResponseDto
                .builder()
                .durationDays(SubscriptionPlan.PRO.getDurationDays())
                .price(SubscriptionPlan.PRO.getPrice()/100)
                .build();

        SubscriptionPlanResponseDto basic = SubscriptionPlanResponseDto
                .builder()
                .durationDays(SubscriptionPlan.BASIC.getDurationDays())
                .price(SubscriptionPlan.BASIC.getPrice()/100)
                .build();

        SubscriptionPlanResponseDto premium = SubscriptionPlanResponseDto
                .builder()
                .durationDays(SubscriptionPlan.PREMIUM.getDurationDays())
                .price(SubscriptionPlan.PREMIUM.getPrice()/100)
                .build();

        return List.of(pro, premium, basic);
    }
}
