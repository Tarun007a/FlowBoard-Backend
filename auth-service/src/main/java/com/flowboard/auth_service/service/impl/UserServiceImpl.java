package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public String deleteById(Integer userId) {
        if(userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return "User Deleted successfully";
        }
        else {
            throw new UserNotFoundException("User with user id " + userId + " does not exist");
        }
    }

    @Override
    public void deactivateAccount(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        user.setActive(false);
    }

    @Override
    public CustomPageResponse<UserDto> findAllByRole(String roleStr, int page, int size, String sortBy, String direction) {
        ROLE role = ROLE.valueOf(roleStr);
        Sort sort = Sort.by(sortBy);

        if(direction.equals("asc")) sort = sort.ascending();
        else sort = sort.descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        log.info(pageable.toString());

        Page<User> userResponsePage = userRepository.findAllByRole(role, pageable);

        Page<UserDto> userDtoCustomPageResponse = userResponsePage.map(userResponseMapper::mapTo);

        return new CustomPageResponse<>(userDtoCustomPageResponse);
    }

    @Override
    public CustomPageResponse<UserDto> searchByFullName(String fullName, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(sortBy);

        if(direction.equals("asc")) sort = sort.ascending();
        else sort = sort.descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userResponsePage = userRepository.searchByFullName(fullName, pageable);
        Page<UserDto> userDtoCustomPageResponse = userResponsePage.map(userResponseMapper::mapTo);

        return new CustomPageResponse<>(userDtoCustomPageResponse);
    }

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
    }
}
