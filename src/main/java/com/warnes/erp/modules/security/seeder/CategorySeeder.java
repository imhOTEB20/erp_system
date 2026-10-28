package com.warnes.erp.modules.security.seeder;

import com.warnes.erp.modules.catalog.entity.Category;

import com.warnes.erp.modules.catalog.repository.CategoryRepository;
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
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Iniciando inyección de datos semilla (Categoria)...");

            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("categories.json").getInputStream();
                List<Map<String, String>> categoriesData = mapper.readValue(inputStream, new TypeReference<List<Map<String,String>>>() {});

                // Convertimos la lista de nombres en entidades Brand
                List<Category> categories = categoriesData.stream()
                        .map(categoryData -> {
                            Category category = new Category();
                            category.setName(categoryData.get("name"));
                            category.setDescription(categoryData.get("description"));
                            return category;
                        })
                        .toList();

                // Guardamos todas las marcas en una sola operación
                categoryRepository.saveAll(categories);

                log.info("Se insertaron {} categorias desde categories.json", categories.size());
            } catch (Exception e) {
                log.error("Error al cargar las categorias desde categories.json", e);
            }
        } else {
            log.info("Base de datos ya inicializada. Omitiendo CategoriesSeeder.");
        }
    }


}
