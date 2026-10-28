package com.warnes.erp.modules.security.seeder;

import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.entity.TaxType;
import com.warnes.erp.modules.catalog.repository.BrandRepository;

import com.warnes.erp.modules.catalog.repository.TaxTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
public class TaxTypeSeeder implements CommandLineRunner {

    private final TaxTypeRepository taxTypeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (taxTypeRepository.count() == 0) {
            log.info("Iniciando inyección de datos semilla (Impuestos)...");

            TaxType exempt = new TaxType();
            exempt.setName("Exento");
            exempt.setDefaultRate(BigDecimal.ZERO);

            taxTypeRepository.save(exempt);
        } else {
            log.info("Base de datos ya inicializada. Omitiendo Seeder.");
        }
    }
}
