package com.warnes.erp.modules.security.seeder;

import com.warnes.erp.modules.cash.entity.CashRegister;
import com.warnes.erp.modules.cash.repository.CashRegisterRepository;
import com.warnes.erp.modules.catalog.entity.TaxType;

import com.warnes.erp.modules.catalog.repository.TaxTypeRepository;
import com.warnes.erp.modules.inventory.entity.Warehouse;
import com.warnes.erp.modules.inventory.entity.WarehouseType;
import com.warnes.erp.modules.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(5)
public class WarehouseSeeder implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final CashRegisterRepository cashRegisterRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (warehouseRepository.count() == 0) {
            log.info("Iniciando inyección de datos semilla (Sucursales)...");

            Warehouse mainWarehouse = new Warehouse();
            mainWarehouse.setName("Repuesteria - El Carril");
            mainWarehouse.setCode("T-ELCARRIL01");
            mainWarehouse.setType(WarehouseType.STORE);

            mainWarehouse = warehouseRepository.save(mainWarehouse);

            CashRegister mainCashRegister = new CashRegister();
            mainCashRegister.setName("CAJA01");
            mainCashRegister.setWarehouse(mainWarehouse);

            cashRegisterRepository.save(mainCashRegister);

        } else {
            log.info("Base de datos ya inicializada. Omitiendo WarehouseSeeder.");
        }
    }
}
