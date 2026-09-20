package com.warnes.erp.modules.security.service;

import com.warnes.erp.modules.security.dto.request.AuthRequestDTO;
import com.warnes.erp.modules.security.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO authenticate(AuthRequestDTO request);
}