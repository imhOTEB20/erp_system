package com.warnes.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.ZonedDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "zonedDateTimeProvider")
public class JpaAuditingConfig {

    /**
     * Provee la fecha y hora actual con zona horaria (ZonedDateTime)
     * para que coincida con los campos de AuditableEntity y el TIMESTAMPTZ de PostgreSQL.
     */
    @Bean
    public DateTimeProvider zonedDateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now());
    }
}