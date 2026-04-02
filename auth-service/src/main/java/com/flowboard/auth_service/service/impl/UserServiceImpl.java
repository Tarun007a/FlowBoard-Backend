package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Mapper<User, UserDto> userResponseMapper;

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        return userResponseMapper.mapTo(user);
    }

    @Override
    public UserDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        return userResponseMapper.mapTo(user);
    }

    @Override
    public UserDto updateProfile(Integer id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        user.setFullName(userUpdateDto.getFullName());
        user.setAvatarUrl(userUpdateDto.getAvatarUrl());
        User updatedUser = userRepository.save(user);

        return userResponseMapper.mapTo(updatedUser);
    }

    @Override
    public void deactivateAccount(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        user.setActive(false);
    }
}
