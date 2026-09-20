package com.warnes.erp.modules.security.service;

import com.warnes.erp.modules.security.dto.request.UserCreateRequestDTO;
import com.warnes.erp.modules.security.dto.request.UserUpdateRequestDTO;
import com.warnes.erp.modules.security.dto.response.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(UserCreateRequestDTO request);
    UserResponseDTO updateUser(UUID uuid, UserUpdateRequestDTO request);
    UserResponseDTO getUserByUuid(UUID uuid);
    List<UserResponseDTO> getAllUsers();
    void deleteUser(UUID uuid);
}