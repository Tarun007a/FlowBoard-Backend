package com.flowboard.card_service.controller;

import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.service.CardActivityService;
import com.flowboard.card_service.util.AppConstants;
import com.flowboard.card_service.util.CustomPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardActivityController {

    private final CardActivityService cardActivityService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @GetMapping("/card/{cardId}")
    public CustomPageResponse<CardActivityResponseDto> getByCard(
            @PathVariable Integer cardId,
            @RequestParam(defaultValue = AppConstants.page) int page,
            @RequestParam(defaultValue = AppConstants.size) int size,
            @RequestParam(defaultValue = AppConstants.sort) String sortBy,
            @RequestParam(defaultValue = AppConstants.direction) String direction,
            HttpServletRequest request) {

        Integer userId = extractUserId(request);

        return cardActivityService.getActivitiesByCard(
                cardId,
                page,
                size,
                sortBy,
                direction,
                userId
        );
    }
}