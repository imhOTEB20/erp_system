package com.warnes.erp.modules.sales.controller;

import com.warnes.erp.common.ApiResponse;
import com.warnes.erp.modules.sales.dto.request.CustomerPaymentRequestDTO;
import com.warnes.erp.modules.sales.dto.request.CustomerRequestDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerLedgerResponseDTO;
import com.warnes.erp.modules.sales.dto.response.CustomerResponseDTO;
import com.warnes.erp.modules.sales.service.CustomerLedgerService;
import com.warnes.erp.modules.sales.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerLedgerService customerLedgerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> create(@Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente creado exitosamente"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers()));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByUuid(uuid)));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO response = customerService.updateCustomer(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cliente actualizado exitosamente"));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')") // ALTA SENSIBILIDAD: Solo el Admin borra clientes lógicamente
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID uuid) {
        customerService.deleteCustomer(uuid);
        return ResponseEntity.ok(ApiResponse.success("Cliente eliminado correctamente (Soft Delete)"));
    }

    @GetMapping("/{uuid}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<CustomerLedgerResponseDTO>>> getCustomerLedger(@PathVariable UUID uuid) {
        List<CustomerLedgerResponseDTO> ledger = customerLedgerService.getLedgerByCustomerUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(ledger));
    }

    @PostMapping("/{uuid}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<Void>> payDebt(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerPaymentRequestDTO request) {
        customerService.registerDebtPayment(uuid, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Pago de cuenta corriente registrado exitosamente."));
    }
}