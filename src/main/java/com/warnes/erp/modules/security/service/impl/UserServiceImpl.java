package com.warnes.erp.modules.security.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.exception.ResourceNotFoundException;
import com.warnes.erp.modules.security.dto.request.UserCreateRequestDTO;
import com.warnes.erp.modules.security.dto.request.UserUpdateRequestDTO;
import com.warnes.erp.modules.security.dto.response.UserResponseDTO;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.mapper.UserMapper;
import com.warnes.erp.modules.security.repository.UserRepository;
import com.warnes.erp.modules.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new BusinessRuleException("El nombre de usuario '" + request.username() + "' ya está en uso.");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID uuid, UserUpdateRequestDTO request) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(request.active());

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUuid(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Soft delete: Evita borrar los registros de auditoría (ventas, cajas) vinculados a este usuario
        user.setDeletedAt(ZonedDateTime.now());
        user.setActive(false);
        userRepository.save(user);
    }
}