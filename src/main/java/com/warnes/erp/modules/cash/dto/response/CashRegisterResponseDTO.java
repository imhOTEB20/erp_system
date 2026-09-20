package com.warnes.erp.modules.cash.dto.response;

import java.util.UUID;

public record CashRegisterResponseDTO(
        UUID uuid,
        String name,
        boolean active
) {}