package com.flowboard.workspace_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE")
public interface UserClient {
    @GetMapping("api/v1/user/check/{userId}")
    public Boolean checkUser(@PathVariable(value = "userId") Integer userId);
}
