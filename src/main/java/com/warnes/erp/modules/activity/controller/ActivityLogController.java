package com.warnes.erp.modules.activity.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.activity.dto.response.ActivityLogResponseDTO;
import com.warnes.erp.modules.activity.dto.response.AdminDashboardSummaryDTO;
import com.warnes.erp.modules.activity.entity.ActivityType;
import com.warnes.erp.modules.activity.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Actividades Recientes y Auditoría Operativa", description = "Feed paginado con filtros por sucursal, cliente y caja")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar actividades recientes paginadas con filtros por sucursal, cliente y caja")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponseDTO>>> getRecentActivities(
            @RequestParam(required = false) UUID warehouseUuid,
            @RequestParam(required = false) UUID customerUuid,
            @RequestParam(required = false) UUID cashRegisterUuid,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ActivityLogResponseDTO> page = activityLogService.getRecentActivities(
                warehouseUuid, customerUuid, cashRegisterUuid, module, activityType, from, to, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(page, "Actividades recientes obtenidas con éxito"));
    }

    @GetMapping("/admin-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener resumen ejecutivo y últimas 20 actividades para el Administrador")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryDTO>> getAdminSummary(
            @RequestParam(required = false) UUID warehouseUuid
    ) {
        AdminDashboardSummaryDTO summary = activityLogService.getAdminDashboardSummary(warehouseUuid);
        return ResponseEntity.ok(ApiResponse.success(summary, "Resumen administrativo obtenido con éxito"));
    }
}