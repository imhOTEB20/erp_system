package com.warnes.erp.modules.security.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.security.dto.request.UserCreateRequestDTO;
import com.warnes.erp.modules.security.dto.request.UserUpdateRequestDTO;
import com.warnes.erp.modules.security.dto.response.UserResponseDTO;
import com.warnes.erp.modules.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Seguridad - Usuarios", description = "Administración de empleados, roles y acceso al sistema")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Registrar nuevo usuario", description = "Da de alta a un empleado en el sistema y encripta su contraseña. Exclusivo para administradores.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Usuario creado exitosamente"));
    }

    @Operation(summary = "Actualizar usuario", description = "Modifica el rol, el nombre completo o suspende (activa/inactiva) el acceso de un empleado al sistema.")
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @Parameter(description = "UUID del usuario a modificar") @PathVariable UUID uuid,
            @Valid @RequestBody UserUpdateRequestDTO request) {
        UserResponseDTO response = userService.updateUser(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario actualizado exitosamente"));
    }

    @Operation(summary = "Consultar detalle de usuario")
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByUuid(
            @Parameter(description = "UUID del usuario") @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByUuid(uuid)));
    }

    @Operation(summary = "Listar todos los usuarios", description = "Devuelve la nómina completa de empleados registrados.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @Operation(summary = "Dar de baja usuario", description = "Ejecuta un borrado lógico (Soft Delete) impidiendo que el empleado inicie sesión en el futuro.")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "UUID del usuario a dar de baja") @PathVariable UUID uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.ok(ApiResponse.success("Usuario dado de baja exitosamente"));
    }
}