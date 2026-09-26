package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.modules.catalog.dto.request.KitItemRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.KitItemResponseDTO;
import com.warnes.erp.modules.catalog.service.KitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{parentUuid}/kit-items")
@RequiredArgsConstructor
@Tag(name = "Catálogo - Kits y Combos", description = "Gestión de componentes para productos marcados como Kit (ej: Kit de Distribución)")
public class KitController {

    private final KitService kitService;

    @Operation(summary = "Listar componentes del Kit", description = "Obtiene los repuestos individuales que arman este producto.")
    @GetMapping
    public ResponseEntity<List<KitItemResponseDTO>> getKitItems(@PathVariable UUID parentUuid) {
        return ResponseEntity.ok(kitService.getKitItems(parentUuid));
    }

    @Operation(summary = "Agregar o actualizar componente", description = "Vincula un repuesto unitario a este Kit padre.")
    @PostMapping
    public ResponseEntity<List<KitItemResponseDTO>> addOrUpdateKitItem(
            @PathVariable UUID parentUuid,
            @Valid @RequestBody KitItemRequestDTO request) {
        return ResponseEntity.ok(kitService.addOrUpdateKitItem(parentUuid, request));
    }

    @Operation(summary = "Remover componente del Kit")
    @DeleteMapping("/{childUuid}")
    public ResponseEntity<Void> removeKitItem(
            @PathVariable UUID parentUuid,
            @PathVariable UUID childUuid) {
        kitService.removeKitItem(parentUuid, childUuid);
        return ResponseEntity.noContent().build();
    }
}