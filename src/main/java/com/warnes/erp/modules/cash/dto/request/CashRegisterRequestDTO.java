package com.warnes.erp.modules.cash.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CashRegisterRequestDTO(
        @NotBlank(message = "El nombre de la caja es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String name
) {}