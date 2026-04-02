package com.flowboard.auth_service.service;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;

public interface UserService {
    public UserDto getUserByEmail(String email);

    public UserDto getUserById(Integer id);

    public UserDto updateProfile(Integer id, UserUpdateDto userUpdateDto);

    public void deactivateAccount(Integer id);
}
