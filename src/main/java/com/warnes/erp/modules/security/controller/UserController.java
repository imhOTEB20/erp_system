package com.warnes.erp.modules.security.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.security.dto.request.UserCreateRequestDTO;
import com.warnes.erp.modules.security.dto.request.UserUpdateRequestDTO;
import com.warnes.erp.modules.security.dto.response.UserResponseDTO;
import com.warnes.erp.modules.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Restringe todo el controlador a nivel de seguridad
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Usuario creado exitosamente"));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable UUID uuid,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        UserResponseDTO response = userService.updateUser(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario actualizado exitosamente"));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByUuid(uuid)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.ok(ApiResponse.success("Usuario dado de baja exitosamente"));
    }
}