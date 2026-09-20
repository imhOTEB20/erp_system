package com.warnes.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Aquí a futuro podemos inyectar un AuditorAware si queremos guardar qué usuario hizo la modificación
}