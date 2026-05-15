package com.flowboard.subscription_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.subscription_service.controller.SubscriptionController;
import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void buySubscription_missingHeader_returns400() throws Exception {

        mockMvc.perform(post("/api/v1/subscriptions/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buySubscription_invalidHeader_returns400() throws Exception {

        mockMvc.perform(post("/api/v1/subscriptions/buy")
                        .header("X-User-Id", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void verifyPayment_negative() throws Exception {

        when(subscriptionService.verifyAndActivate(any(), anyInt()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/subscriptions/verify")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMySubscription_positive() throws Exception {

        when(subscriptionService.getDetails(1))
                .thenReturn(new SubscriptionResponseDto());

        mockMvc.perform(get("/api/v1/subscriptions/my")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void getPlanDetails_positive() throws Exception {

        when(subscriptionService.getPlanDetails())
                .thenReturn(List.of(
                        new SubscriptionPlanResponseDto()
                ));

        mockMvc.perform(get("/api/v1/subscriptions/details"))
                .andExpect(status().isOk());
    }

    @Test
    void isSubscribed_positive() throws Exception {

        when(subscriptionService.isSubscribed(1))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/subscriptions/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isSubscribed_false() throws Exception {

        when(subscriptionService.isSubscribed(1))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/subscriptions/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}