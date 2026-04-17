package com.flowboard.comment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "CARD-CLIENT")
public interface CardClient {
    @GetMapping("/assigned-user/{cardId}")
    public Integer getAssignedUserId(Integer cardId);
}
