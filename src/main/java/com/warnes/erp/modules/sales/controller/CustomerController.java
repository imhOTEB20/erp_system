package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.CustomerPaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.request.CustomerRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerLedgerResponseDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerResponseDTO;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.sales.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Ventas - Clientes", description = "Gestión de fichas de clientes, saldos de cuenta corriente y libro mayor")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerLedgerService customerLedgerService;

    @Operation(summary = "Crear Cliente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> create(@Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente creado exitosamente"));
    }

    @Operation(summary = "Listar Clientes")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers()));
    }

    @Operation(summary = "Consultar Cliente")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByUuid(uuid)));
    }

    @Operation(summary = "Actualizar Cliente")
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO response = customerService.updateCustomer(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cliente actualizado exitosamente"));
    }

    @Operation(summary = "Eliminar Cliente", description = "Inhabilita al cliente impidiendo que se le emitan nuevas facturas.")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        customerService.deleteCustomer(uuid);
        return ResponseEntity.ok(ApiResponse.success("Cliente eliminado correctamente (Soft Delete)"));
    }

    @Operation(summary = "Consultar Libro Mayor", description = "Retorna el extracto de cuenta corriente (Customer Ledger) histórico del cliente.")
    @GetMapping("/{uuid}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CustomerLedgerResponseDTO>>> getCustomerLedger(@PathVariable UUID uuid) {
        List<CustomerLedgerResponseDTO> ledger = customerLedgerService.getLedgerByCustomerUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(ledger));
    }

    @Operation(summary = "Saldar Deuda de Cuenta Corriente", description = "Registra un ingreso de dinero y disminuye el pasivo del cliente en el Libro Mayor.")
    @PostMapping("/{uuid}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<Void>> payDebt(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerPaymentRequestDTO request) {
        customerService.registerDebtPayment(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Pago de cuenta corriente registrado exitosamente."));
    }
}