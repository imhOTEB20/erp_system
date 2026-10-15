package com.warnes.erp.modules.security.seeder;

import com.warnes.erp.modules.catalog.entity.Brand;
import com.warnes.erp.modules.catalog.repository.BrandRepository;

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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class BrandSeeder implements CommandLineRunner {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (brandRepository.count() == 0) {
            log.info("Iniciando inyección de datos semilla (Marcas)...");

            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("brands.json").getInputStream();
                List<String> brandNames = mapper.readValue(inputStream, new TypeReference<List<String>>() {});

                // Convertimos la lista de nombres en entidades Brand
                List<Brand> brands = brandNames.stream()
                        .map(name -> {
                            Brand brand = new Brand();
                            brand.setName(name);
                            return brand;
                        })
                        .collect(Collectors.toList());

                // Guardamos todas las marcas en una sola operación
                brandRepository.saveAll(brands);

                log.info("Se insertaron {} marcas desde brands.json", brands.size());
            } catch (Exception e) {
                log.error("Error al cargar marcas desde brands.json", e);
            }
        } else {
            log.info("Base de datos ya inicializada. Omitiendo Seeder.");
        }
    }


}
