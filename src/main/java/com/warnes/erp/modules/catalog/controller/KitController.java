package com.warnes.erp.modules.catalog.controller;

import com.warnes.erp.modules.catalog.dto.request.KitItemRequestDTO;
import com.warnes.erp.modules.catalog.dto.response.KitItemResponseDTO;
import com.warnes.erp.modules.catalog.service.KitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{parentUuid}/kit-items")
@RequiredArgsConstructor
public class KitController {

    private final KitService kitService;

    @GetMapping
    public ResponseEntity<List<KitItemResponseDTO>> getKitItems(@PathVariable UUID parentUuid) {
        return ResponseEntity.ok(kitService.getKitItems(parentUuid));
    }

    @PostMapping
    public ResponseEntity<List<KitItemResponseDTO>> addOrUpdateKitItem(
            @PathVariable UUID parentUuid,
            @Valid @RequestBody KitItemRequestDTO request) {
        return ResponseEntity.ok(kitService.addOrUpdateKitItem(parentUuid, request));
    }

    @DeleteMapping("/{childUuid}")
    public ResponseEntity<Void> removeKitItem(
            @PathVariable UUID parentUuid,
            @PathVariable UUID childUuid) {
        kitService.removeKitItem(parentUuid, childUuid);
        return ResponseEntity.noContent().build();
    }
}