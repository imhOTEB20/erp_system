package com.warnes.erp.modules.security.service.impl;

import com.warnes.erp.exception.BusinessRuleException;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.security.dto.request.AuthRequestDTO;
import com.warnes.erp.modules.security.dto.response.AuthResponseDTO;
import com.warnes.erp.modules.security.entity.User;
import com.warnes.erp.modules.security.repository.UserRepository;
import com.warnes.erp.modules.security.service.AuthService;
import com.warnes.erp.modules.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        // Spring Security valida la contraseña contra el hash de la BD automáticamente
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessRuleException("Usuario no encontrado"));

        if (!user.isActive()) {
            throw new BusinessRuleException("El usuario se encuentra inactivo");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("uuid", user.getUuid().toString());

        String jwtToken = jwtService.generateToken(extraClaims, user);

        Warehouse userWarehouse = user.getDefaultWarehouse();
        UUID warehouseId = userWarehouse != null ? userWarehouse.getUuid() : null;
        String warehouseName = userWarehouse != null ? userWarehouse.getName() : null;

        return new AuthResponseDTO(
                jwtToken,
                user.getUuid(),
                user.getFullName(),
                user.getRole().name(),
                warehouseId,
                warehouseName
        );
    }
}