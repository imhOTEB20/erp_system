package com.warnes.erp.modules.security.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.security.dto.request.AuthRequestDTO;
import com.warnes.erp.modules.security.dto.response.AuthResponseDTO;
import com.warnes.erp.modules.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Autenticación exitosa"));
    }
}